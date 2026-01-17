package com.moirai.alloc.gantt.query.application;

import com.moirai.alloc.common.port.ProjectInfoPort;
import com.moirai.alloc.common.port.ProjectMembershipPort;
import com.moirai.alloc.gantt.common.exception.GanttException;
import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import com.moirai.alloc.gantt.query.dto.projection.MilestoneProjection;
import com.moirai.alloc.gantt.query.dto.projection.TaskProjection;
import com.moirai.alloc.gantt.query.dto.request.TaskSearchRequest;
import com.moirai.alloc.gantt.query.dto.response.MilestoneResponse;
import com.moirai.alloc.gantt.query.dto.response.TaskResponse;
import com.moirai.alloc.gantt.query.mapper.MilestoneQueryMapper;
import com.moirai.alloc.gantt.query.mapper.TaskQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GanttQueryService {

    private final ProjectMembershipPort projectMembershipPort;
    private final ProjectInfoPort projectInfoPort;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TaskQueryMapper taskQueryMapper;
    private final MilestoneQueryMapper milestoneQueryMapper;

    public GanttQueryService(ProjectMembershipPort projectMembershipPort,
                             ProjectInfoPort projectInfoPort,
                             AuthenticatedUserProvider authenticatedUserProvider,
                             TaskQueryMapper taskQueryMapper,
                             MilestoneQueryMapper milestoneQueryMapper) {
        this.projectMembershipPort = projectMembershipPort;
        this.projectInfoPort = projectInfoPort;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.taskQueryMapper = taskQueryMapper;
        this.milestoneQueryMapper = milestoneQueryMapper;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findTasks(Long projectId, TaskSearchRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        validateProject(projectId);
        validateMember(projectId, userId);

        List<TaskProjection> tasks = taskQueryMapper.findTasks(
                projectId,
                userId,
                request.status() == null ? null : request.status().name(),
                request.startDate(),
                request.endDate()
        );

        return tasks.stream()
                .map(this::toTaskResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MilestoneResponse findMilestone(Long projectId, Long milestoneId) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        validateProject(projectId);
        validateMember(projectId, userId);

        List<MilestoneProjection> milestones = milestoneQueryMapper.findMilestones(projectId);
        MilestoneProjection milestone = milestones.stream()
                .filter(item -> item.milestoneId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> GanttException.notFound("마일스톤이 존재하지 않습니다."));

        List<TaskResponse> tasks = taskQueryMapper.findTasks(projectId, null, null, null, null)
                .stream()
                .filter(task -> task.milestoneId().equals(milestoneId))
                .map(this::toTaskResponse)
                .toList();

        return toMilestoneResponse(milestone, tasks);
    }

    @Transactional(readOnly = true)
    public List<MilestoneResponse> findMilestones(Long projectId) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        validateProject(projectId);
        validateMember(projectId, userId);

        List<MilestoneProjection> milestones = milestoneQueryMapper.findMilestones(projectId);
        List<TaskResponse> allTasks = taskQueryMapper.findTasks(projectId, null, null, null, null)
                .stream()
                .map(this::toTaskResponse)
                .toList();

        Map<Long, List<TaskResponse>> tasksByMilestone = allTasks.stream()
                .collect(Collectors.groupingBy(TaskResponse::milestoneId));

        return milestones.stream()
                .map(milestone -> toMilestoneResponse(
                        milestone,
                        tasksByMilestone.getOrDefault(milestone.milestoneId(), List.of())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Double findMilestoneCompletionRate(Long projectId) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        validateProject(projectId);
        validateMember(projectId, userId);

        List<Boolean> completionStates = milestoneQueryMapper.findMilestoneCompletionStates(projectId);
        if (completionStates.isEmpty()) {
            return 0.0;
        }

        long completedCount = completionStates.stream()
                .filter(Boolean.TRUE::equals)
                .count();

        return completedCount * 100.0 / completionStates.size();
    }

    private void validateMember(Long projectId, Long userId) {
        if (!projectMembershipPort.isMember(projectId, userId)) {
            throw GanttException.notFound("프로젝트 멤버가 아닙니다.");
        }
    }

    private void validateProject(Long projectId) {
        if (projectInfoPort.findProjectPeriod(projectId).isEmpty()) {
            throw GanttException.notFound("프로젝트가 존재하지 않습니다.");
        }
    }

    private TaskResponse toTaskResponse(TaskProjection projection) {
        return new TaskResponse(
                projection.taskId(),
                projection.milestoneId(),
                projection.userName(),
                projection.taskCategory(),
                projection.taskName(),
                projection.taskDescription(),
                projection.taskStatus(),
                projection.createdAt(),
                projection.updatedAt(),
                projection.startDate(),
                projection.endDate(),
                projection.isCompleted(),
                projection.isDeleted()
        );
    }

    private MilestoneResponse toMilestoneResponse(MilestoneProjection milestone, List<TaskResponse> tasks) {
        return new MilestoneResponse(
                milestone.milestoneId(),
                milestone.projectId(),
                milestone.milestoneName(),
                milestone.createdAt(),
                milestone.updatedAt(),
                milestone.startDate(),
                milestone.endDate(),
                milestone.achievementRate(),
                milestone.isDeleted(),
                tasks
        );
    }
}
