package com.moirai.alloc.report.command.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import com.moirai.alloc.report.command.domain.entity.IssueBlocker;
import com.moirai.alloc.report.command.domain.entity.WeeklyReport;
import com.moirai.alloc.report.command.domain.entity.WeeklyTask;
import com.moirai.alloc.report.command.dto.CreateWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.IncompleteTaskRequest;
import com.moirai.alloc.report.command.dto.NextWeekTaskRequest;
import com.moirai.alloc.report.command.dto.UpdateWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.WeeklyReportSaveResponse;
import com.moirai.alloc.report.command.repository.IssueBlockerCommandRepository;
import com.moirai.alloc.report.command.repository.WeeklyReportCommandRepository;
import com.moirai.alloc.report.command.repository.WeeklyTaskCommandRepository;
import com.moirai.alloc.report.query.dto.WeeklyReportCreateResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.repository.ReportMembershipRepository;
import com.moirai.alloc.report.query.repository.WeeklyReportQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WeeklyReportCommandService {

    private final WeeklyReportCommandRepository weeklyReportCommandRepository;
    private final WeeklyTaskCommandRepository weeklyTaskCommandRepository;
    private final IssueBlockerCommandRepository issueBlockerCommandRepository;
    private final ReportMembershipRepository membershipRepository;
    private final WeeklyReportQueryRepository weeklyReportQueryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public WeeklyReportCommandService(WeeklyReportCommandRepository weeklyReportCommandRepository,
                                      WeeklyTaskCommandRepository weeklyTaskCommandRepository,
                                      IssueBlockerCommandRepository issueBlockerCommandRepository,
                                      ReportMembershipRepository membershipRepository,
                                      WeeklyReportQueryRepository weeklyReportQueryRepository) {
        this.weeklyReportCommandRepository = weeklyReportCommandRepository;
        this.weeklyTaskCommandRepository = weeklyTaskCommandRepository;
        this.issueBlockerCommandRepository = issueBlockerCommandRepository;
        this.membershipRepository = membershipRepository;
        this.weeklyReportQueryRepository = weeklyReportQueryRepository;
    }

    @Transactional
    public WeeklyReportCreateResponse createWeeklyReport(CreateWeeklyReportRequest request,
                                                         UserPrincipal principal) {
        validateMembership(request.projectId(), principal.userId());
        WeeklyReport report = WeeklyReport.create(
                principal.userId(),
                request.projectId(),
                request.weekStartDate(),
                request.weekEndDate()
        );
        WeeklyReport saved = weeklyReportCommandRepository.save(report);
        WeeklyReportDetailResponse detail = weeklyReportQueryRepository.findDetail(saved.getReportId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다."));
        return new WeeklyReportCreateResponse(
                detail.reportId(),
                detail.projectId(),
                detail.projectName(),
                detail.weekStartDate(),
                detail.weekEndDate(),
                detail.reportStatus(),
                detail.taskCompletionRate(),
                detail.summaryText(),
                detail.completedTasks(),
                detail.incompleteTasks(),
                detail.nextWeekTasks()
        );
    }

    @Transactional
    public WeeklyReportSaveResponse updateWeeklyReport(UpdateWeeklyReportRequest request,
                                                       UserPrincipal principal) {
        WeeklyReport report = weeklyReportCommandRepository.findByReportIdAndIsDeletedFalse(request.reportId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다."));
        validateMembership(report.getProjectId(), principal.userId());
        validateOwnerOrPm(report, principal);

        report.updateReport(request.reportStatus(), request.changeOfPlan(), request.taskCompletionRate());

        issueBlockerCommandRepository.deleteByWeeklyTaskReportReportId(report.getReportId());
        weeklyTaskCommandRepository.deleteByReportReportId(report.getReportId());

        if (request.completedTasks() != null) {
            request.completedTasks().forEach(taskRequest -> {
                Task task = entityManager.getReference(Task.class, taskRequest.taskId());
                WeeklyTask weeklyTask = WeeklyTask.create(report, task, WeeklyTask.TaskType.COMPLETED, null, null);
                weeklyTaskCommandRepository.save(weeklyTask);
            });
        }

        if (request.incompleteTasks() != null) {
            request.incompleteTasks().forEach(taskRequest -> {
                Task task = entityManager.getReference(Task.class, taskRequest.taskId());
                WeeklyTask weeklyTask = WeeklyTask.create(report, task, WeeklyTask.TaskType.INCOMPLETE, null, null);
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
                        taskRequest.plannedEndDate()
                );
                weeklyTaskCommandRepository.save(weeklyTask);
            });
        }

        return new WeeklyReportSaveResponse(report.getReportId(), report.getUpdatedAt());
    }

    @Transactional
    public void deleteWeeklyReport(Long reportId, UserPrincipal principal) {
        WeeklyReport report = weeklyReportCommandRepository.findByReportIdAndIsDeletedFalse(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다."));
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
}
