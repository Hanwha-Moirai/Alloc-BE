package com.moirai.alloc.report.command.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import com.moirai.alloc.report.command.domain.entity.IssueBlocker;
import com.moirai.alloc.report.command.domain.entity.WeeklyReport;
import com.moirai.alloc.report.command.domain.entity.WeeklyTask;
import com.moirai.alloc.report.command.dto.request.IncompleteTaskRequest;
import com.moirai.alloc.report.command.dto.request.UpdateWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.response.WeeklyReportSaveResponse;
import com.moirai.alloc.report.command.repository.IssueBlockerCommandRepository;
import com.moirai.alloc.report.command.repository.WeeklyReportCommandRepository;
import com.moirai.alloc.report.command.repository.WeeklyTaskCommandRepository;
import com.moirai.alloc.report.query.dto.WeeklyReportCreateResponse;
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

@Service
public class WeeklyReportCommandService {

    private final WeeklyReportCommandRepository weeklyReportCommandRepository;
    private final WeeklyTaskCommandRepository weeklyTaskCommandRepository;
    private final IssueBlockerCommandRepository issueBlockerCommandRepository;
    private final ReportMembershipRepository membershipRepository;
    private final WeeklyReportQueryRepository weeklyReportQueryRepository;
    private final NotificationCommandService notificationCommandService;

    @PersistenceContext
    private EntityManager entityManager;

    public WeeklyReportCommandService(WeeklyReportCommandRepository weeklyReportCommandRepository,
                                      WeeklyTaskCommandRepository weeklyTaskCommandRepository,
                                      IssueBlockerCommandRepository issueBlockerCommandRepository,
                                      ReportMembershipRepository membershipRepository,
                                      WeeklyReportQueryRepository weeklyReportQueryRepository,
                                      NotificationCommandService notificationCommandService) {
        this.weeklyReportCommandRepository = weeklyReportCommandRepository;
        this.weeklyTaskCommandRepository = weeklyTaskCommandRepository;
        this.issueBlockerCommandRepository = issueBlockerCommandRepository;
        this.membershipRepository = membershipRepository;
        this.weeklyReportQueryRepository = weeklyReportQueryRepository;
        this.notificationCommandService = notificationCommandService;
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
        WeeklyReport saved = weeklyReportCommandRepository.save(report);
        WeeklyReportDetailResponse detail = weeklyReportQueryRepository.findDetail(saved.getReportId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다."));
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

        report.updateReport(request.reportStatus(), request.changeOfPlan(), request.taskCompletionRate());

        issueBlockerCommandRepository.deleteByWeeklyTaskReportReportId(report.getReportId());
        weeklyTaskCommandRepository.deleteByReportReportId(report.getReportId());

        // request의 완수 task 목록이 비어 있는 것이 아니라면
        if (request.completedTasks() != null) {
            request.completedTasks().forEach(taskRequest -> {
                Task task = entityManager.getReference(Task.class, taskRequest.taskId());
                WeeklyTask weeklyTask = WeeklyTask.create(
                        report,
                        task,
                        WeeklyTask.TaskType.COMPLETED,
                        null,
                        null,
                        task.getIsCompleted()
                );
                weeklyTaskCommandRepository.save(weeklyTask);
            });
        }

        // request의 미완수 task 목록이 비어 있다면
        if (request.incompleteTasks() != null) {
            request.incompleteTasks().forEach(taskRequest -> {
                Task task = entityManager.getReference(Task.class, taskRequest.taskId());
                WeeklyTask weeklyTask = WeeklyTask.create(
                        report,
                        task,
                        WeeklyTask.TaskType.INCOMPLETE,
                        null,
                        null,
                        task.getIsCompleted()
                );
                WeeklyTask savedTask = weeklyTaskCommandRepository.save(weeklyTask);
                createIssueBlocker(savedTask, taskRequest);
            });
        }

        if (request.nextWeekTasks() != null) {
            request.nextWeekTasks().forEach(taskRequest -> {
                Task task = entityManager.getReference(Task.class, taskRequest.taskId());
                WeeklyTask weeklyTask = WeeklyTask.create(
                        report,
                        task,
                        WeeklyTask.TaskType.NEXT_WEEK,
                        taskRequest.plannedStartDate(),
                        taskRequest.plannedEndDate(),
                        task.getIsCompleted()
                );
                weeklyTaskCommandRepository.save(weeklyTask);
            });
        }

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
    }

    private void createIssueBlocker(WeeklyTask weeklyTask, IncompleteTaskRequest request) {
        if (request.delayReason() == null) {
            return;
        }
        IssueBlocker blocker = IssueBlocker.create(
                weeklyTask,
                request.delayReason(),
                null,
                null
        );
        issueBlockerCommandRepository.save(blocker);
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
