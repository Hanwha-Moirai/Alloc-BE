package com.moirai.alloc.report.command.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import com.moirai.alloc.gantt.query.dto.projection.TaskProjection;
import com.moirai.alloc.gantt.query.mapper.TaskQueryMapper;
import com.moirai.alloc.report.command.domain.entity.IssueBlocker;
import com.moirai.alloc.report.command.domain.entity.WeeklyReport;
import com.moirai.alloc.report.command.domain.entity.WeeklyReportLog;
import com.moirai.alloc.report.command.domain.entity.WeeklyTask;
import com.moirai.alloc.report.command.dto.request.IncompleteTaskRequest;
import com.moirai.alloc.report.command.dto.request.UpdateWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.response.WeeklyReportSaveResponse;
import com.moirai.alloc.report.command.repository.IssueBlockerCommandRepository;
import com.moirai.alloc.report.command.repository.WeeklyReportCommandRepository;
import com.moirai.alloc.report.command.repository.WeeklyReportLogRepository;
import com.moirai.alloc.report.command.repository.WeeklyTaskCommandRepository;
import com.moirai.alloc.report.command.dto.response.WeeklyReportCreateResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.repository.ReportMembershipRepository;
import com.moirai.alloc.report.query.repository.WeeklyReportQueryRepository;
import com.moirai.alloc.notification.command.service.NotificationCommandService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeeklyReportCommandService {

    private final WeeklyReportCommandRepository weeklyReportCommandRepository;
    private final WeeklyTaskCommandRepository weeklyTaskCommandRepository;
    private final IssueBlockerCommandRepository issueBlockerCommandRepository;
    private final ReportMembershipRepository membershipRepository;
    private final WeeklyReportQueryRepository weeklyReportQueryRepository;
    private final WeeklyReportLogRepository weeklyReportLogRepository;
    private final NotificationCommandService notificationCommandService;
    private final TaskQueryMapper taskQueryMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public WeeklyReportCommandService(WeeklyReportCommandRepository weeklyReportCommandRepository,
                                      WeeklyTaskCommandRepository weeklyTaskCommandRepository,
                                      IssueBlockerCommandRepository issueBlockerCommandRepository,
                                      ReportMembershipRepository membershipRepository,
                                      WeeklyReportQueryRepository weeklyReportQueryRepository,
                                      WeeklyReportLogRepository weeklyReportLogRepository,
                                      NotificationCommandService notificationCommandService,
                                      TaskQueryMapper taskQueryMapper) {
        this.weeklyReportCommandRepository = weeklyReportCommandRepository;
        this.weeklyTaskCommandRepository = weeklyTaskCommandRepository;
        this.issueBlockerCommandRepository = issueBlockerCommandRepository;
        this.membershipRepository = membershipRepository;
        this.weeklyReportQueryRepository = weeklyReportQueryRepository;
        this.weeklyReportLogRepository = weeklyReportLogRepository;
        this.notificationCommandService = notificationCommandService;
        this.taskQueryMapper = taskQueryMapper;
    }

    @Transactional
    public WeeklyReportCreateResponse createWeeklyReport(Long projectId,
                                                         UserPrincipal principal) {
        validateMembership(projectId, principal.userId());
        LocalDate reportDate = LocalDate.now();
        WeeklyReport report = WeeklyReport.create(
                principal.userId(),
                projectId,
                reportDate,
                reportDate
        );
        Double taskCompletionRate = calculateTaskCompletionRate(projectId, reportDate);
        report.updateReport(null, null, taskCompletionRate);
        WeeklyReport saved = weeklyReportCommandRepository.save(report);
        saveWeeklyTasksSnapshot(saved);
        WeeklyReportDetailResponse detail = weeklyReportQueryRepository.findDetail(saved.getReportId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다."));
        logWeeklyReportChange(saved, principal.userId(), WeeklyReportLog.ActionType.CREATE,
                formatCreateMessage(detail.weekStartDate(), detail.weekEndDate(), detail.weekLabel()));
        //notifyWeeklyReportCreated(detail.reportId(), detail.projectId(), detail.reporterName(), principal.userId());
        return new WeeklyReportCreateResponse(
                detail.reportId(),
                detail.projectId(),
                detail.projectName(),
                detail.reporterName(),
                detail.weekStartDate(),
                detail.weekEndDate(),
                detail.weekLabel(),
                detail.reportStatus(),
                detail.taskCompletionRate(),
                detail.summaryText(),
                detail.completedTasks(),
                detail.incompleteTasks(),
                detail.nextWeekTasks()
        );
    }

    @Transactional
    public WeeklyReportSaveResponse updateWeeklyReport(Long projectId,
                                                       UpdateWeeklyReportRequest request,
                                                       UserPrincipal principal) {
        WeeklyReport report = weeklyReportCommandRepository.findByReportIdAndIsDeletedFalse(request.reportId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다."));
        if (!report.getProjectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다.");
        }
        validateMembership(report.getProjectId(), principal.userId());
        validateOwnerOrPm(report, principal);

        String updateMessage = buildWeeklyReportUpdateMessage(report, request);

        report.updateReport(request.reportStatus(), request.changeOfPlan(), null);

        // request의 완수 task 목록이 null이 아니라면 해당 카테고리만 갱신
        if (request.completedTasks() != null) {
            weeklyTaskCommandRepository.deleteByReportReportIdAndTaskType(
                    report.getReportId(),
                    WeeklyTask.TaskType.COMPLETED
            );
            request.completedTasks().forEach(taskRequest -> {
                Task task = entityManager.getReference(Task.class, taskRequest.taskId());
                boolean isCompleted = true;
                WeeklyTask weeklyTask = WeeklyTask.create(
                        report,
                        task,
                        WeeklyTask.TaskType.COMPLETED,
                        null,
                        null,
                        isCompleted
                );
                weeklyTaskCommandRepository.save(weeklyTask);
            });
        }

        // request의 미완수 task 목록이 null이 아니라면 해당 카테고리만 갱신
        if (request.incompleteTasks() != null) {
            issueBlockerCommandRepository.deleteByWeeklyTaskReportReportId(report.getReportId());
            weeklyTaskCommandRepository.deleteByReportReportIdAndTaskType(
                    report.getReportId(),
                    WeeklyTask.TaskType.INCOMPLETE
            );
            request.incompleteTasks().forEach(taskRequest -> {
                Task task = entityManager.getReference(Task.class, taskRequest.taskId());
                boolean isCompleted = false;
                WeeklyTask weeklyTask = WeeklyTask.create(
                        report,
                        task,
                        WeeklyTask.TaskType.INCOMPLETE,
                        null,
                        null,
                        isCompleted
                );
                WeeklyTask savedTask = weeklyTaskCommandRepository.save(weeklyTask);
                createIssueBlocker(savedTask, taskRequest);
            });
        }

        if (request.nextWeekTasks() != null) {
            weeklyTaskCommandRepository.deleteByReportReportIdAndTaskType(
                    report.getReportId(),
                    WeeklyTask.TaskType.NEXT_WEEK
            );
            request.nextWeekTasks().forEach(taskRequest -> {
                Task task = entityManager.getReference(Task.class, taskRequest.taskId());
                boolean isCompleted = Task.TaskStatus.DONE.equals(task.getTaskStatus())
                        || Boolean.TRUE.equals(task.getIsCompleted());
                WeeklyTask weeklyTask = WeeklyTask.create(
                        report,
                        task,
                        WeeklyTask.TaskType.NEXT_WEEK,
                        taskRequest.plannedStartDate(),
                        taskRequest.plannedEndDate(),
                        isCompleted
                );
                weeklyTaskCommandRepository.save(weeklyTask);
            });
        }

        logWeeklyReportChange(report, principal.userId(), WeeklyReportLog.ActionType.UPDATE, updateMessage);

        return new WeeklyReportSaveResponse(report.getReportId(), report.getUpdatedAt());
    }

    @Transactional
    public void deleteWeeklyReport(Long projectId, Long reportId, UserPrincipal principal) {
        WeeklyReport report = weeklyReportCommandRepository.findByReportIdAndIsDeletedFalse(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다."));
        if (!report.getProjectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다.");
        }
        validateMembership(report.getProjectId(), principal.userId());
        validateOwnerOrPm(report, principal);
        report.markDeleted();
        logWeeklyReportChange(report, principal.userId(), WeeklyReportLog.ActionType.DELETE,
                "주간보고 삭제");
    }

    private void logWeeklyReportChange(WeeklyReport report, Long actorUserId,
                                       WeeklyReportLog.ActionType actionType, String message) {
        WeeklyReportLog log = WeeklyReportLog.builder()
                .projectId(report.getProjectId())
                .reportId(report.getReportId())
                .actorUserId(actorUserId)
                .actionType(actionType)
                .logMessage(message)
                .build();
        weeklyReportLogRepository.save(log);
    }

    private String formatCreateMessage(LocalDate start, LocalDate end, String weekLabel) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String period = String.format("%s~%s", start.format(fmt), end.format(fmt));
        return "주간보고 생성: " + weekLabel + " (" + period + ")";
    }

    private String buildWeeklyReportUpdateMessage(WeeklyReport report, UpdateWeeklyReportRequest request) {
        List<String> parts = new ArrayList<>();

        if (request.reportStatus() != null && request.reportStatus() != report.getReportStatus()) {
            parts.add("상태 " + report.getReportStatus() + " → " + request.reportStatus());
        }

        if (request.changeOfPlan() != null) {
            String before = report.getChangeOfPlan();
            String after = request.changeOfPlan();
            if (before == null || before.isBlank()) {
                if (!after.isBlank()) {
                    parts.add("변경사항 추가");
                }
            } else if (after == null || after.isBlank()) {
                parts.add("변경사항 삭제");
            } else if (!before.equals(after)) {
                parts.add("변경사항 수정");
            }
        }

        Integer completed = request.completedTasks() != null ? request.completedTasks().size() : null;
        Integer incomplete = request.incompleteTasks() != null ? request.incompleteTasks().size() : null;
        Integer nextWeek = request.nextWeekTasks() != null ? request.nextWeekTasks().size() : null;
        if (completed != null || incomplete != null || nextWeek != null) {
            parts.add("태스크 " +
                    "완료 " + (completed == null ? "-" : completed) + ", " +
                    "미완료 " + (incomplete == null ? "-" : incomplete) + ", " +
                    "다음주 " + (nextWeek == null ? "-" : nextWeek));
        }

        if (parts.isEmpty()) {
            return "주간보고 수정";
        }
        return "주간보고 수정: " + String.join(", ", parts);
    }

    private void createIssueBlocker(WeeklyTask weeklyTask, IncompleteTaskRequest request) {
        Integer delayedDates = calculateDelayedDates(weeklyTask.getReport(), weeklyTask.getTask());
        IssueBlocker blocker = IssueBlocker.create(
                weeklyTask,
                request.delayReason(),
                null,
                delayedDates
        );
        issueBlockerCommandRepository.save(blocker);
    }

    private void createIssueBlocker(WeeklyTask weeklyTask) {
        Integer delayedDates = calculateDelayedDates(weeklyTask.getReport(), weeklyTask.getTask());
        IssueBlocker blocker = IssueBlocker.create(
                weeklyTask,
                null,
                null,
                delayedDates
        );
        issueBlockerCommandRepository.save(blocker);
    }

    private void saveWeeklyTasksSnapshot(WeeklyReport report) {
        List<TaskProjection> tasks = taskQueryMapper.findTasks(
                report.getProjectId(),
                null,
                null,
                null,
                null,
                null
        );
        for (TaskProjection projection : tasks) {
            Task task = entityManager.getReference(Task.class, projection.taskId());
            boolean isCompleted = Boolean.TRUE.equals(projection.isCompleted())
                    || Task.TaskStatus.DONE.equals(projection.taskStatus());
            WeeklyTask.TaskType taskType = isCompleted
                    ? WeeklyTask.TaskType.COMPLETED
                    : WeeklyTask.TaskType.INCOMPLETE;
            WeeklyTask weeklyTask = WeeklyTask.create(
                    report,
                    task,
                    taskType,
                    null,
                    null,
                    isCompleted
            );
            WeeklyTask savedTask = weeklyTaskCommandRepository.save(weeklyTask);
            if (taskType == WeeklyTask.TaskType.INCOMPLETE) {
                createIssueBlocker(savedTask);
            }
        }
    }

    private Integer calculateDelayedDates(WeeklyReport report, Task task) {
        if (report.getWeekEndDate() == null || task.getEndDate() == null) {
            return null;
        }
        long diff = java.time.temporal.ChronoUnit.DAYS.between(task.getEndDate(), report.getWeekEndDate());
        if (diff <= 0) {
            return 0;
        }
        if (diff > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) diff;
    }

    private Double calculateTaskCompletionRate(Long projectId, LocalDate reportDate) {
        List<TaskProjection> dueTasks = taskQueryMapper.findTasks(
                projectId,
                null,
                null,
                null,
                reportDate,
                null
        );
        int total = dueTasks.size();
        if (total == 0) {
            return 0.0;
        }
        long completed = dueTasks.stream()
                .filter(task -> Boolean.TRUE.equals(task.isCompleted())
                        || Task.TaskStatus.DONE.equals(task.taskStatus()))
                .count();
        return completed / (double) total;
    }

    private void validateMembership(Long projectId, Long userId) {
        if (!membershipRepository.existsMembership(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "프로젝트 멤버가 아닙니다.");
        }
    }

    private void validateOwnerOrPm(WeeklyReport report, UserPrincipal principal) {
        boolean isPm = "PM".equalsIgnoreCase(principal.role());
        boolean isOwner = report.getUserId().equals(principal.userId());
        if (!isPm && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }
    }

    /*
    private void notifyWeeklyReportCreated(Long reportId, Long projectId, String reporterName, Long userId) {
        InternalNotificationCreateRequest request = InternalNotificationCreateRequest.of(
                AlarmTemplateType.WEEKLY_REPORT,
                java.util.List.of(userId),
                java.util.Map.of("weeklyReportName", reporterName),
                TargetType.WEEKLY_REPORT,
                reportId,
                "/projects/" + projectId + "/docs/report/" + reportId
        );
        notificationCommandService.createInternalNotifications(request);
    }
     */
}
