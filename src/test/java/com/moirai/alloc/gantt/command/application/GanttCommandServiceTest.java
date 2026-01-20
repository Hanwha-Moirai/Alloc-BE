package com.moirai.alloc.gantt.command.application;

import com.moirai.alloc.gantt.command.application.dto.request.CreateMilestoneRequest;
import com.moirai.alloc.gantt.command.application.dto.request.CreateTaskRequest;
import com.moirai.alloc.gantt.command.application.dto.request.UpdateTaskRequest;
import com.moirai.alloc.gantt.command.application.service.GanttCommandService;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import com.moirai.alloc.gantt.command.domain.repository.MilestoneRepository;
import com.moirai.alloc.gantt.command.domain.repository.TaskRepository;
import com.moirai.alloc.gantt.command.domain.repository.TaskUpdateLogRepository;
import com.moirai.alloc.gantt.common.exception.GanttException;
import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("local")
@Sql(scripts = "/sql/gantt/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/gantt/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class GanttCommandServiceTest {

    private static final Long PROJECT_ID = 99001L;
    private static final Long USER_ID = 99001L;
    private static final Long ASSIGNEE_ID = 99002L;
    private static final Long MILESTONE_ID = 99001L;

    @Autowired
    private GanttCommandService ganttCommandService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private TaskUpdateLogRepository taskUpdateLogRepository;

    @Autowired
    private TestAuthenticatedUserProvider authenticatedUserProvider;

    @Test
    @DisplayName("태스크 생성에 성공했습니다.")
    void createTask_savesTaskAndLog() {
        CreateTaskRequest request = new CreateTaskRequest(
                MILESTONE_ID,
                ASSIGNEE_ID,
                Task.TaskCategory.DEVELOPMENT,
                "TEST_TASK_CREATE_99001",
                "desc",
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 3)
        );

        Long taskId = ganttCommandService.createTask(PROJECT_ID, request);

        Task task = taskRepository.findById(taskId).orElseThrow();
        assertThat(task.getTaskName()).isEqualTo("TEST_TASK_CREATE_99001");

        boolean hasLog = taskUpdateLogRepository.findAll().stream()
                .anyMatch(log -> log.getTaskId().equals(taskId) && "CREATE".equals(log.getUpdateReason()));
        assertThat(hasLog).isTrue();
    }

    @Test
    @DisplayName("태스크 수정에 성공하면 변경된 값이 저장된다.")
    void updateTask_updatesFields() {
        authenticatedUserProvider.setRole("PM");
        UpdateTaskRequest request = new UpdateTaskRequest(
                null,
                ASSIGNEE_ID,
                Task.TaskCategory.TESTING,
                "UPDATED_TASK_99001",
                "updated",
                null,
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 4)
        );

        ganttCommandService.updateTask(PROJECT_ID, 99001L, request);

        Task task = taskRepository.findById(99001L).orElseThrow();
        assertThat(task.getTaskName()).isEqualTo("UPDATED_TASK_99001");
        assertThat(task.getTaskCategory()).isEqualTo(Task.TaskCategory.TESTING);
        assertThat(task.getTaskStatus()).isEqualTo(Task.TaskStatus.TODO);
    }

    @Test
    @DisplayName("프로젝트 멤버가 아닌 담당자로 변경 시 예외가 발생한다.")
    void updateTask_whenAssigneeNotMember_throwsNotFound() {
        authenticatedUserProvider.setRole("PM");
        UpdateTaskRequest request = new UpdateTaskRequest(
                null,
                99999L,
                null,
                null,
                null,
                null,
                null,
                null
        );

        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.updateTask(PROJECT_ID, 99001L, request)
        );

        assertThat(exception.getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("프로젝트 기간을 벗어난 태스크 수정은 거부된다.")
    void updateTask_whenOutOfProjectPeriod_throwsBadRequest() {
        authenticatedUserProvider.setRole("PM");
        UpdateTaskRequest request = new UpdateTaskRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDate.of(2025, 3, 1)
        );

        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.updateTask(PROJECT_ID, 99001L, request)
        );

        assertThat(exception.getCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("마일스톤 기간을 벗어난 태스크 수정은 거부된다.")
    void updateTask_whenOutOfMilestonePeriod_throwsBadRequest() {
        authenticatedUserProvider.setRole("PM");
        UpdateTaskRequest request = new UpdateTaskRequest(
                99002L,
                null,
                null,
                null,
                null,
                null,
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 4)
        );

        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.updateTask(PROJECT_ID, 99001L, request)
        );

        assertThat(exception.getCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("태스크 삭제 시 삭제 플래그가 설정된다.")
    void deleteTask_marksDeleted() {
        ganttCommandService.deleteTask(PROJECT_ID, 99001L);

        Task task = taskRepository.findById(99001L).orElseThrow();
        assertThat(task.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("이미 삭제된 태스크는 삭제할 수 없다.")
    void deleteTask_whenAlreadyDeleted_throwsNotFound() {
        ganttCommandService.deleteTask(PROJECT_ID, 99001L);

        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.deleteTask(PROJECT_ID, 99001L)
        );

        assertThat(exception.getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("태스크 담당자는 상태 변경이 가능하다.")
    void updateTask_whenAssignee_updatesStatus() {
        authenticatedUserProvider.setRole("USER");
        authenticatedUserProvider.setUserId(ASSIGNEE_ID);

        UpdateTaskRequest request = new UpdateTaskRequest(
                null,
                null,
                null,
                null,
                null,
                Task.TaskStatus.INPROGRESS,
                null,
                null
        );

        ganttCommandService.updateTask(PROJECT_ID, 99001L, request);

        Task task = taskRepository.findById(99001L).orElseThrow();
        assertThat(task.getTaskStatus()).isEqualTo(Task.TaskStatus.INPROGRESS);
        assertThat(task.getIsCompleted()).isFalse();
    }

    @Test
    @DisplayName("태스크 담당자가 완료 상태로 변경하면 완료 플래그가 설정된다.")
    void updateTask_whenAssigneeMarksDone_setsCompleted() {
        authenticatedUserProvider.setRole("USER");
        authenticatedUserProvider.setUserId(ASSIGNEE_ID);

        UpdateTaskRequest request = new UpdateTaskRequest(
                null,
                null,
                null,
                null,
                null,
                Task.TaskStatus.DONE,
                null,
                null
        );

        ganttCommandService.updateTask(PROJECT_ID, 99001L, request);

        Task task = taskRepository.findById(99001L).orElseThrow();
        assertThat(task.getTaskStatus()).isEqualTo(Task.TaskStatus.DONE);
        assertThat(task.getIsCompleted()).isTrue();
    }

    @Test
    @DisplayName("마일스톤 생성에 성공한다.")
    void createMilestone_succeeds() {
        Long milestoneId = ganttCommandService.createMilestone(PROJECT_ID, new CreateMilestoneRequest(
                "NEW_MILESTONE_99001",
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 12),
                0L
        ));

        assertThat(milestoneId).isNotNull();
        assertThat(milestoneRepository.findById(milestoneId)).isPresent();
    }

    @Test
    @DisplayName("마일스톤 수정이 성공한다.")
    void updateMilestone_succeeds() {
        ganttCommandService.updateMilestone(PROJECT_ID, MILESTONE_ID, new com.moirai.alloc.gantt.command.application.dto.request.UpdateMilestoneRequest(
                "UPDATED_MILESTONE_99001",
                null,
                null,
                80L
        ));

        var milestone = milestoneRepository.findById(MILESTONE_ID).orElseThrow();
        assertThat(milestone.getMilestoneName()).isEqualTo("UPDATED_MILESTONE_99001");
        assertThat(milestone.getAchievementRate()).isEqualTo(80L);
    }

    @Test
    @DisplayName("하위 태스크 기간을 포함하지 않는 마일스톤 수정은 거부된다.")
    void updateMilestone_whenScheduleInvalid_throwsBadRequest() {
        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.updateMilestone(PROJECT_ID, MILESTONE_ID, new com.moirai.alloc.gantt.command.application.dto.request.UpdateMilestoneRequest(
                        null,
                        LocalDate.of(2025, 1, 4),
                        LocalDate.of(2025, 1, 10),
                        null
                ))
        );

        assertThat(exception.getCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("하위 태스크가 없는 마일스톤은 삭제할 수 있다.")
    void deleteMilestone_whenNoTasks_succeeds() {
        Long milestoneId = ganttCommandService.createMilestone(PROJECT_ID, new CreateMilestoneRequest(
                "EMPTY_MILESTONE_99001",
                LocalDate.of(2025, 1, 20),
                LocalDate.of(2025, 1, 25),
                0L
        ));

        ganttCommandService.deleteMilestone(PROJECT_ID, milestoneId);

        var milestone = milestoneRepository.findById(milestoneId).orElseThrow();
        assertThat(milestone.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("태스크 담당자가 아니면 상태 변경이 거부된다.")
    void updateTask_whenNotAssignee_throwsForbidden() {
        authenticatedUserProvider.setRole("USER");
        authenticatedUserProvider.setUserId(USER_ID);

        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.updateTask(PROJECT_ID, 99001L, new UpdateTaskRequest(
                        null,
                        null,
                        null,
                        null,
                        null,
                        Task.TaskStatus.INPROGRESS,
                        null,
                        null
                ))
        );
        assertThat(exception.getCode()).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("태스크 담당자는 내용 변경을 할 수 없다.")
    void updateTask_whenAssigneeChangesContent_throwsForbidden() {
        authenticatedUserProvider.setRole("USER");
        authenticatedUserProvider.setUserId(ASSIGNEE_ID);

        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.updateTask(PROJECT_ID, 99001L, new UpdateTaskRequest(
                        null,
                        null,
                        null,
                        "CONTENT_CHANGE",
                        null,
                        null,
                        null,
                        null
                ))
        );
        assertThat(exception.getCode()).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("PM은 태스크 상태 변경이 허용되지 않는다.")
    void updateTask_whenPmChangesStatus_throwsForbidden() {
        authenticatedUserProvider.setRole("PM");

        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.updateTask(PROJECT_ID, 99001L, new UpdateTaskRequest(
                        null,
                        null,
                        null,
                        null,
                        null,
                        Task.TaskStatus.INPROGRESS,
                        null,
                        null
                ))
        );
        assertThat(exception.getCode()).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("하위 태스크가 있으면 마일스톤 삭제가 거부된다.")
    void deleteMilestone_whenHasTasks_throwsConflict() {
        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.deleteMilestone(PROJECT_ID, MILESTONE_ID)
        );

        assertThat(exception.getCode()).isEqualTo("CONFLICT");
    }


    @TestConfiguration
    static class TestAuthConfig {
        @Bean
        @Primary
        TestAuthenticatedUserProvider authenticatedUserProvider() {
            return new TestAuthenticatedUserProvider();
        }
    }

    static class TestAuthenticatedUserProvider implements AuthenticatedUserProvider {
        private Long userId = USER_ID;
        private String role = "PM";

        void setUserId(Long userId) {
            this.userId = userId;
        }

        void setRole(String role) {
            this.role = role;
        }

        @Override
        public Long getCurrentUserId() {
            return userId;
        }

        @Override
        public String getCurrentUserRole() {
            return role;
        }
    }
}
