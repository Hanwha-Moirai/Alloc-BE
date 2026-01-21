package com.moirai.alloc.gantt.command.application;

import com.moirai.alloc.common.port.ProjectInfoPort;
import com.moirai.alloc.common.port.ProjectMembershipPort;
import com.moirai.alloc.common.port.ProjectPeriod;
import com.moirai.alloc.gantt.command.application.dto.request.CreateTaskRequest;
import com.moirai.alloc.gantt.command.application.dto.request.UpdateTaskRequest;
import com.moirai.alloc.gantt.command.application.service.GanttCommandService;
import com.moirai.alloc.gantt.command.domain.entity.Milestone;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import com.moirai.alloc.gantt.command.domain.repository.MilestoneRepository;
import com.moirai.alloc.gantt.command.domain.repository.TaskRepository;
import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import com.moirai.alloc.gantt.query.dto.projection.TaskProjection;
import com.moirai.alloc.gantt.query.mapper.TaskQueryMapper;
import com.moirai.alloc.notification.command.service.NotificationCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("local")
class MilestoneCompletionTriggerTest {

    private static final Long USER_ID = 99101L;
    private static final Long PROJECT_ID = 99100L;

    @Autowired
    private GanttCommandService ganttCommandService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @MockitoBean
    private ProjectInfoPort projectInfoPort;

    @MockitoBean
    private ProjectMembershipPort projectMembershipPort;

    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    @MockitoBean
    private TaskQueryMapper taskQueryMapper;

    @MockitoBean
    private NotificationCommandService notificationCommandService;

    @BeforeEach
    void setUp() {
        when(projectInfoPort.findProjectPeriod(PROJECT_ID))
                .thenReturn(Optional.of(new ProjectPeriod(
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2025, 12, 31)
                )));
        when(projectMembershipPort.isMember(PROJECT_ID, USER_ID)).thenReturn(true);
        when(authenticatedUserProvider.getCurrentUserRole()).thenReturn("USER");
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(USER_ID);
    }

    @Test
    @DisplayName("태스크가 모두 완료면 마일스톤 완료로 자동 처리된다.")
    void insertDoneTask_marksMilestoneCompleted() {
        Milestone milestone = milestoneRepository.save(buildMilestone("Trigger M1"));

        Long taskId = ganttCommandService.createTask(PROJECT_ID, buildCreateRequest(milestone.getMilestoneId()));
        Task task = taskRepository.findById(taskId).orElseThrow();
        when(taskQueryMapper.findTaskById(PROJECT_ID, taskId)).thenReturn(buildProjection(task));

        ganttCommandService.updateTask(PROJECT_ID, taskId, buildStatusRequest(Task.TaskStatus.DONE));

        Milestone updated = milestoneRepository.findById(milestone.getMilestoneId()).orElseThrow();
        assertThat(updated.getIsCompleted()).isTrue();
    }

    @Test
    @DisplayName("미완료 태스크가 있으면 마일스톤은 완료되지 않는다.")
    void insertTodoTask_keepsMilestoneIncomplete() {
        Milestone milestone = milestoneRepository.save(buildMilestone("Trigger M2"));

        ganttCommandService.createTask(PROJECT_ID, buildCreateRequest(milestone.getMilestoneId()));

        // 3. 해당 마일스톤에 미완료된 상태에 있는 태스크가 있음
        Milestone updated = milestoneRepository.findById(milestone.getMilestoneId()).orElseThrow();
        assertThat(updated.getIsCompleted()).isFalse();
    }

    @Test
    @DisplayName("마지막 미완료 태스크가 완료되면 마일스톤이 완료된다.")
    void updateTaskStatus_toDoneCompletesMilestone() {
        Milestone milestone = milestoneRepository.save(buildMilestone("Trigger M3"));

        Long firstTaskId = ganttCommandService.createTask(PROJECT_ID, buildCreateRequest(milestone.getMilestoneId()));
        Long secondTaskId = ganttCommandService.createTask(PROJECT_ID, buildCreateRequest(milestone.getMilestoneId()));
        Task firstTask = taskRepository.findById(firstTaskId).orElseThrow();
        Task secondTask = taskRepository.findById(secondTaskId).orElseThrow();
        when(taskQueryMapper.findTaskById(PROJECT_ID, firstTaskId)).thenReturn(buildProjection(firstTask));
        when(taskQueryMapper.findTaskById(PROJECT_ID, secondTaskId)).thenReturn(buildProjection(secondTask));

        // 3. 완료된 태스크의
        Milestone beforeUpdate = milestoneRepository.findById(milestone.getMilestoneId()).orElseThrow();
        assertThat(beforeUpdate.getIsCompleted()).isFalse();

        ganttCommandService.updateTask(PROJECT_ID, firstTaskId, buildStatusRequest(Task.TaskStatus.DONE));
        Milestone afterFirstUpdate = milestoneRepository.findById(milestone.getMilestoneId()).orElseThrow();
        assertThat(afterFirstUpdate.getIsCompleted()).isFalse();

        ganttCommandService.updateTask(PROJECT_ID, secondTaskId, buildStatusRequest(Task.TaskStatus.DONE));

        Milestone updated = milestoneRepository.findById(milestone.getMilestoneId()).orElseThrow();
        assertThat(updated.getIsCompleted()).isTrue();
    }

    @Test
    @DisplayName("마일스톤의 태스크가 없으면 완료되지 않는다.")
    void deleteLastTask_marksMilestoneIncomplete() {
        Milestone milestone = milestoneRepository.save(buildMilestone("Trigger M4"));

        Long taskId = ganttCommandService.createTask(PROJECT_ID, buildCreateRequest(milestone.getMilestoneId()));
        Task task = taskRepository.findById(taskId).orElseThrow();
        when(taskQueryMapper.findTaskById(PROJECT_ID, taskId)).thenReturn(buildProjection(task));
        ganttCommandService.updateTask(PROJECT_ID, taskId, buildStatusRequest(Task.TaskStatus.DONE));

        Milestone beforeDelete = milestoneRepository.findById(milestone.getMilestoneId()).orElseThrow();
        assertThat(beforeDelete.getIsCompleted()).isTrue();

        ganttCommandService.deleteTask(PROJECT_ID, taskId);

        Milestone updated = milestoneRepository.findById(milestone.getMilestoneId()).orElseThrow();
        assertThat(updated.getIsCompleted()).isFalse();
    }

    private Milestone buildMilestone(String name) {
        return Milestone.builder()
                .projectId(PROJECT_ID)
                .milestoneName(name)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 10))
                .achievementRate(0L)
                .build();
    }

    private CreateTaskRequest buildCreateRequest(Long milestoneId) {
        return new CreateTaskRequest(
                milestoneId,
                USER_ID,
                Task.TaskCategory.DEVELOPMENT,
                "TRIGGER_TASK_TODO",
                "trigger test",
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 3)
        );
    }

    private UpdateTaskRequest buildStatusRequest(Task.TaskStatus status) {
        return new UpdateTaskRequest(
                null,
                null,
                null,
                null,
                null,
                status,
                null,
                null
        );
    }

    private TaskProjection buildProjection(Task task) {
        return new TaskProjection(
                task.getTaskId(),
                task.getMilestone().getMilestoneId(),
                null,
                task.getTaskCategory(),
                task.getTaskName(),
                task.getTaskDescription(),
                task.getTaskStatus(),
                null,
                null,
                task.getStartDate(),
                task.getEndDate(),
                task.getIsCompleted(),
                task.getIsDeleted()
        );
    }

}
