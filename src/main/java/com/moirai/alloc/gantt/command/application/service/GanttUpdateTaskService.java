package com.moirai.alloc.gantt.command.application.service;

import com.moirai.alloc.common.port.ProjectInfoPort;
import com.moirai.alloc.common.port.ProjectMembershipPort;
import com.moirai.alloc.common.port.ProjectPeriod;
import com.moirai.alloc.gantt.command.application.dto.request.UpdateTaskRequest;
import com.moirai.alloc.gantt.command.domain.entity.Milestone;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import com.moirai.alloc.gantt.command.domain.entity.TaskUpdateLog;
import com.moirai.alloc.gantt.command.domain.repository.MilestoneRepository;
import com.moirai.alloc.gantt.command.domain.repository.TaskRepository;
import com.moirai.alloc.gantt.command.domain.repository.TaskUpdateLogRepository;
import com.moirai.alloc.gantt.common.exception.GanttException;
import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import com.moirai.alloc.gantt.query.dto.projection.TaskProjection;
import com.moirai.alloc.gantt.query.mapper.TaskQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Service
public class GanttUpdateTaskService {

    private final ProjectInfoPort projectInfoPort;
    private final ProjectMembershipPort projectMembershipPort;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final MilestoneRepository milestoneRepository;
    private final TaskRepository taskRepository;
    private final TaskQueryMapper taskQueryMapper;
    private final TaskUpdateLogRepository taskUpdateLogRepository;

    public GanttUpdateTaskService(ProjectInfoPort projectInfoPort,
                                  ProjectMembershipPort projectMembershipPort,
                                  AuthenticatedUserProvider authenticatedUserProvider,
                                  MilestoneRepository milestoneRepository,
                                  TaskRepository taskRepository,
                                  TaskQueryMapper taskQueryMapper,
                                  TaskUpdateLogRepository taskUpdateLogRepository) {
        this.projectInfoPort = projectInfoPort;
        this.projectMembershipPort = projectMembershipPort;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.milestoneRepository = milestoneRepository;
        this.taskRepository = taskRepository;
        this.taskQueryMapper = taskQueryMapper;
        this.taskUpdateLogRepository = taskUpdateLogRepository;
    }

    @Transactional
    public void updateTask(Long projectId, Long taskId, UpdateTaskRequest request) {
        validateProject(projectId);

        Task task = findTaskWithinProject(projectId, taskId);
        Long previousMilestoneId = task.getMilestone() == null ? null : task.getMilestone().getMilestoneId();
        if (Boolean.TRUE.equals(task.getIsDeleted())) {
            throw GanttException.notFound("태스크가 존재하지 않습니다.");
        }

        String role = authenticatedUserProvider.getCurrentUserRole();
        boolean isPm = "PM".equalsIgnoreCase(role);

        if (isPm) {
            handlePmUpdate(projectId, task, request, previousMilestoneId);
            return;
        }

        handleUserStatusUpdate(task, request, previousMilestoneId);
    }

    private void handlePmUpdate(Long projectId, Task task, UpdateTaskRequest request, Long previousMilestoneId) {
        Task.TaskStatus targetStatus = request.taskStatus() == null
                ? task.getTaskStatus()
                : request.taskStatus();

        Long previousAssigneeId = task.getUserId();
        Long assigneeId = request.assigneeId() == null ? task.getUserId() : request.assigneeId();
        if (!Objects.equals(task.getUserId(), assigneeId)) {
            validateProjectMember(projectId, assigneeId);
        }

        Milestone targetMilestone = task.getMilestone();
        if (request.milestoneId() != null && !Objects.equals(targetMilestone.getMilestoneId(), request.milestoneId())) {
            targetMilestone = milestoneRepository.findByMilestoneIdAndProjectId(request.milestoneId(), projectId)
                    .orElseThrow(() -> GanttException.notFound("마일스톤이 존재하지 않습니다."));
            validateWithinMilestonePeriod(targetMilestone, task.getStartDate(), task.getEndDate());
        }

        Task.TaskCategory taskCategory = request.taskCategory() == null ? task.getTaskCategory() : request.taskCategory();
        String taskName = request.taskName() == null ? task.getTaskName() : request.taskName();
        String taskDescription = request.taskDescription() == null ? task.getTaskDescription() : request.taskDescription();
        LocalDate startDate = requireNonNullElse(request.startDate(), task.getStartDate(), "startDate");
        LocalDate endDate = requireNonNullElse(request.endDate(), task.getEndDate(), "endDate");

        validateWithinProjectPeriod(projectId, startDate, endDate);
        validateWithinMilestonePeriod(targetMilestone, startDate, endDate);

        task.updateTask(
                targetMilestone,
                assigneeId,
                taskCategory,
                taskName,
                taskDescription,
                targetStatus,
                startDate,
                endDate
        );

        if (request.taskStatus() != null) {
            task.changeStatus(targetStatus);
        }

        taskUpdateLogRepository.save(TaskUpdateLog.create(task.getTaskId(), "UPDATE"));
        if (!Objects.equals(previousMilestoneId, targetMilestone.getMilestoneId())) {
            syncMilestoneCompletion(previousMilestoneId, targetMilestone.getMilestoneId());
        }
        if (!Objects.equals(previousAssigneeId, assigneeId)) {
            //notifyTaskAssignee(projectId, task.getTaskId(), assigneeId, task.getTaskName());
        }
    }

    private void handleUserStatusUpdate(Task task, UpdateTaskRequest request, Long previousMilestoneId) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        if (!Objects.equals(task.getUserId(), userId)) {
            throw GanttException.forbidden("태스크 담당자만 상태를 변경할 수 있습니다.");
        }

        boolean hasContentChange = request.milestoneId() != null
                || request.assigneeId() != null
                || request.taskCategory() != null
                || request.taskName() != null
                || request.taskDescription() != null
                || request.startDate() != null
                || request.endDate() != null;
        if (hasContentChange) {
            throw GanttException.forbidden("태스크 담당자는 상태만 변경할 수 있습니다.");
        }

        if (request.taskStatus() == null) {
            throw GanttException.badRequest("변경할 상태가 없습니다.");
        }

        task.changeStatus(request.taskStatus());
        taskUpdateLogRepository.save(TaskUpdateLog.create(task.getTaskId(), "UPDATE"));
        syncMilestoneCompletion(previousMilestoneId);
    }

    // 프로젝트 존재 여부 검사
    private void validateProject(Long projectId) {
        projectInfoPort.findProjectPeriod(projectId)
                .orElseThrow(() -> GanttException.notFound("프로젝트가 존재하지 않습니다."));
    }

    // 프로젝트 멤버인지를 검사
    private void validateProjectMember(Long projectId, Long userId) {
        if (!projectMembershipPort.isMember(projectId, userId)) {
            throw GanttException.notFound("프로젝트 멤버가 아닙니다.");
        }
    }

    // 유효한 프로젝트인지를 검사
    private void validateWithinProjectPeriod(Long projectId, LocalDate startDate, LocalDate endDate) {
        ProjectPeriod period = projectInfoPort.findProjectPeriod(projectId)
                .orElseThrow(() -> GanttException.notFound("프로젝트가 존재하지 않습니다."));

        if (startDate == null || endDate == null) {
            throw GanttException.badRequest("시작일과 종료일은 필수입니다.");
        }

        if (endDate.isBefore(startDate)) {
            throw GanttException.badRequest("종료일은 시작일보다 빠를 수 없습니다.");
        }

        if (startDate.isBefore(period.startDate()) || endDate.isAfter(period.endDate())) {
            throw GanttException.badRequest("프로젝트 기간을 벗어난 일정입니다.");
        }
    }

    // 마일스톤 기간 검증
    private void validateWithinMilestonePeriod(Milestone milestone, LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(milestone.getStartDate()) || endDate.isAfter(milestone.getEndDate())) {
            throw GanttException.badRequest("마일스톤 기간을 벗어난 일정입니다.");
        }
    }

    // 프로젝트 내 태스크 탐색
    private Task findTaskWithinProject(Long projectId, Long taskId) {
        TaskProjection projection = taskQueryMapper.findTaskById(projectId, taskId);
        if (projection == null) {
            throw GanttException.notFound("태스크가 존재하지 않습니다.");
        }
        return getTaskEntity(projection);
    }

    private Task getTaskEntity(TaskProjection projection) {
        return taskRepository.findById(projection.taskId())
                .orElseThrow(() -> GanttException.notFound("태스크가 존재하지 않습니다."));
    }

    private void syncMilestoneCompletion(Long... milestoneIds) {
        if (milestoneIds == null) {
            return;
        }
        for (Long milestoneId : milestoneIds) {
            if (milestoneId == null) {
                continue;
            }
            long totalTasks = taskRepository.countByMilestone_MilestoneIdAndIsDeletedFalse(milestoneId);
            boolean completed = totalTasks > 0
                    && taskRepository.countByMilestone_MilestoneIdAndIsDeletedFalseAndTaskStatus(
                            milestoneId,
                            Task.TaskStatus.DONE
                    ) == totalTasks;
            milestoneRepository.findById(milestoneId)
                    .ifPresent(milestone -> milestone.changeCompletion(completed));
        }
    }

    private LocalDate requireNonNullElse(LocalDate requestValue, LocalDate fallback, String fieldName) {
        if (requestValue == null) {
            return fallback;
        }
        return requestValue;
    }
}
