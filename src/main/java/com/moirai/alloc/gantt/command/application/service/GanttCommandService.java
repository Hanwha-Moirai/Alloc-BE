package com.moirai.alloc.gantt.command.application.service;

import com.moirai.alloc.common.port.ProjectInfoPort;
import com.moirai.alloc.common.port.ProjectMembershipPort;
import com.moirai.alloc.common.port.ProjectPeriod;
import com.moirai.alloc.gantt.command.application.dto.request.CreateMilestoneRequest;
import com.moirai.alloc.gantt.command.application.dto.request.CreateTaskRequest;
import com.moirai.alloc.gantt.command.application.dto.request.UpdateMilestoneRequest;
import com.moirai.alloc.gantt.command.domain.entity.Milestone;
import com.moirai.alloc.gantt.command.domain.entity.MilestoneUpdateLog;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import com.moirai.alloc.gantt.command.domain.entity.TaskUpdateLog;
import com.moirai.alloc.gantt.command.domain.repository.MilestoneRepository;
import com.moirai.alloc.gantt.command.domain.repository.MilestoneUpdateLogRepository;
import com.moirai.alloc.gantt.command.domain.repository.TaskRepository;
import com.moirai.alloc.gantt.command.domain.repository.TaskUpdateLogRepository;
import com.moirai.alloc.gantt.common.exception.GanttException;
import com.moirai.alloc.gantt.query.dto.projection.TaskProjection;
import com.moirai.alloc.gantt.query.mapper.TaskQueryMapper;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.notification.command.service.NotificationCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class GanttCommandService {

    private final ProjectInfoPort projectInfoPort;
    private final ProjectMembershipPort projectMembershipPort;
    private final MilestoneRepository milestoneRepository;
    private final TaskRepository taskRepository;
    private final TaskQueryMapper taskQueryMapper;
    private final TaskUpdateLogRepository taskUpdateLogRepository;
    private final MilestoneUpdateLogRepository milestoneUpdateLogRepository;
    private final NotificationCommandService notificationCommandService;
    private final SquadAssignmentRepository squadAssignmentRepository;

    public GanttCommandService(ProjectInfoPort projectInfoPort,
                               ProjectMembershipPort projectMembershipPort,
                               MilestoneRepository milestoneRepository,
                               TaskRepository taskRepository,
                               TaskQueryMapper taskQueryMapper,
                               TaskUpdateLogRepository taskUpdateLogRepository,
                               MilestoneUpdateLogRepository milestoneUpdateLogRepository,
                               NotificationCommandService notificationCommandService,
                               SquadAssignmentRepository squadAssignmentRepository) {
        this.projectInfoPort = projectInfoPort;
        this.projectMembershipPort = projectMembershipPort;
        this.milestoneRepository = milestoneRepository;
        this.taskRepository = taskRepository;
        this.taskQueryMapper = taskQueryMapper;
        this.taskUpdateLogRepository = taskUpdateLogRepository;
        this.milestoneUpdateLogRepository = milestoneUpdateLogRepository;
        this.notificationCommandService = notificationCommandService;
        this.squadAssignmentRepository = squadAssignmentRepository;
    }

    @Transactional
    public Long createTask(Long projectId, CreateTaskRequest request) {
        validateProject(projectId);
        validateProjectMember(projectId, requireNonNull(request.assigneeId(), "assigneeId"));

        Milestone milestone = milestoneRepository.findByMilestoneIdAndProjectId(
                        requireNonNull(request.milestoneId(), "milestoneId"),
                        projectId)
                .orElseThrow(() -> GanttException.notFound("마일스톤이 존재하지 않습니다."));

        validateWithinProjectPeriod(projectId, request.startDate(), request.endDate());
        validateWithinMilestonePeriod(milestone, request.startDate(), request.endDate());

        Task task = Task.builder()
                .milestone(milestone)
                .userId(request.assigneeId())
                .taskCategory(request.taskCategory())
                .taskName(requireNonNull(request.taskName(), "taskName"))
                .taskDescription(requireNonNull(request.taskDescription(), "taskDescription"))
                .startDate(requireNonNull(request.startDate(), "startDate"))
                .endDate(requireNonNull(request.endDate(), "endDate"))
                .build();

        taskRepository.save(task);
        taskUpdateLogRepository.save(TaskUpdateLog.create(task.getTaskId(), "CREATE"));
        syncMilestoneCompletion(milestone.getMilestoneId());
        //notifyTaskAssignee(projectId, task.getTaskId(), task.getUserId(), task.getTaskName());
        return task.getTaskId();
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId) {
        validateProject(projectId);

        Task task = findTaskWithinProject(projectId, taskId);
        Long milestoneId = task.getMilestone() == null ? null : task.getMilestone().getMilestoneId();
        if (Boolean.TRUE.equals(task.getIsDeleted())) {
            throw GanttException.notFound("태스크가 존재하지 않습니다.");
        }
        task.softDelete();
        taskUpdateLogRepository.save(TaskUpdateLog.create(task.getTaskId(), "DELETE"));
        syncMilestoneCompletion(milestoneId);
    }

    @Transactional
    public Long createMilestone(Long projectId, CreateMilestoneRequest request) {
        validateProject(projectId);

        validateWithinProjectPeriod(projectId, request.startDate(), request.endDate());

        Milestone milestone = Milestone.builder()
                .projectId(projectId)
                .milestoneName(requireNonNull(request.milestoneName(), "milestoneName"))
                .startDate(requireNonNull(request.startDate(), "startDate"))
                .endDate(requireNonNull(request.endDate(), "endDate"))
                .achievementRate(request.achievementRate())
                .build();

        milestoneRepository.save(milestone);
        milestoneUpdateLogRepository.save(MilestoneUpdateLog.create(milestone.getMilestoneId(), "CREATE"));
        //notifyMilestoneCreated(projectId, milestone.getMilestoneId(), milestone.getMilestoneName());
        return milestone.getMilestoneId();
    }

    @Transactional
    public void updateMilestone(Long projectId, Long milestoneId, UpdateMilestoneRequest request) {
        validateProject(projectId);

        Milestone milestone = milestoneRepository.findByMilestoneIdAndProjectId(milestoneId, projectId)
                .orElseThrow(() -> GanttException.notFound("마일스톤이 존재하지 않습니다."));

        if (request.milestoneName() != null) {
            milestone.changeName(request.milestoneName());
        }

        if (request.startDate() != null || request.endDate() != null) {
            LocalDate startDate = requireNonNullElse(request.startDate(), milestone.getStartDate(), "startDate");
            LocalDate endDate = requireNonNullElse(request.endDate(), milestone.getEndDate(), "endDate");
            validateWithinProjectPeriod(projectId, startDate, endDate);
            validateMilestoneScheduleWithTasks(projectId, milestoneId, startDate, endDate);
            milestone.changeSchedule(startDate, endDate);
        }

        if (request.achievementRate() != null) {
            milestone.changeAchievementRate(request.achievementRate());
        }

        milestoneUpdateLogRepository.save(MilestoneUpdateLog.create(milestone.getMilestoneId(), "UPDATE"));
    }

    @Transactional
    public void deleteMilestone(Long projectId, Long milestoneId) {
        validateProject(projectId);

        Milestone milestone = milestoneRepository.findByMilestoneIdAndProjectId(milestoneId, projectId)
                .orElseThrow(() -> GanttException.notFound("마일스톤이 존재하지 않습니다."));

        boolean hasTasks = taskRepository.existsByMilestone_MilestoneIdAndIsDeletedFalse(milestoneId);
        if (hasTasks) {
            throw GanttException.conflict("하위 태스크가 존재하는 마일스톤은 삭제할 수 없습니다.");
        }

        milestone.softDelete();
        milestoneUpdateLogRepository.save(MilestoneUpdateLog.create(milestone.getMilestoneId(), "DELETE"));
    }

    private void validateProject(Long projectId) {
        projectInfoPort.findProjectPeriod(projectId)
                .orElseThrow(() -> GanttException.notFound("프로젝트가 존재하지 않습니다."));
    }

    private void validateProjectMember(Long projectId, Long userId) {
        if (!projectMembershipPort.isMember(projectId, userId)) {
            throw GanttException.notFound("프로젝트 멤버가 아닙니다.");
        }
    }

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

    private void validateWithinMilestonePeriod(Milestone milestone, LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(milestone.getStartDate()) || endDate.isAfter(milestone.getEndDate())) {
            throw GanttException.badRequest("마일스톤 기간을 벗어난 일정입니다.");
        }
    }

    private void validateMilestoneScheduleWithTasks(Long projectId, Long milestoneId, LocalDate startDate, LocalDate endDate) {
        taskQueryMapper.findTasksByMilestone(projectId, milestoneId).stream()
                .forEach(task -> {
                    if (startDate.isAfter(task.startDate()) || endDate.isBefore(task.endDate())) {
                        throw GanttException.badRequest("하위 태스크 기간을 포함해야 합니다.");
                    }
                });
    }


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

    private String requireNonNull(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw GanttException.badRequest(fieldName + "는 필수입니다.");
        }
        return value;
    }

    private Long requireNonNull(Long value, String fieldName) {
        if (value == null) {
            throw GanttException.badRequest(fieldName + "는 필수입니다.");
        }
        return value;
    }

    private LocalDate requireNonNull(LocalDate value, String fieldName) {
        if (value == null) {
            throw GanttException.badRequest(fieldName + "는 필수입니다.");
        }
        return value;
    }

    private LocalDate requireNonNullElse(LocalDate requestValue, LocalDate fallback, String fieldName) {
        if (requestValue == null) {
            return fallback;
        }
        return requestValue;
    }

    /*
    private void notifyTaskAssignee(Long projectId, Long taskId, Long assigneeId, String taskName) {
        InternalNotificationCreateRequest request = InternalNotificationCreateRequest.of(
                AlarmTemplateType.TASK_ASSIGN,
                List.of(assigneeId),
                Map.of("taskName", taskName),
                TargetType.TASK,
                taskId,
                "/projects/" + projectId + "/tasks"
        );
        notificationCommandService.createInternalNotifications(request);
    }

    private void notifyMilestoneCreated(Long projectId, Long milestoneId, String milestoneName) {
        List<Long> targetUserIds = squadAssignmentRepository.findAssignedByProjectId(projectId).stream()
                .map(assignment -> assignment.getUserId())
                .distinct()
                .toList();
        if (targetUserIds.isEmpty()) {
            return;
        }

        InternalNotificationCreateRequest request = InternalNotificationCreateRequest.of(
                AlarmTemplateType.MILESTONE,
                targetUserIds,
                Map.of("milestoneName", milestoneName),
                TargetType.MILESTONE,
                milestoneId,
                "/projects/" + projectId + "/gantt"
        );
        notificationCommandService.createInternalNotifications(request);
    }

     */
}
