package com.moirai.alloc.gantt.command.application;

import com.moirai.alloc.gantt.command.application.dto.request.UpdateTaskRequest;
import com.moirai.alloc.gantt.command.application.service.GanttUpdateTaskService;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import com.moirai.alloc.gantt.command.domain.repository.TaskRepository;
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
class GanttUpdateTaskServiceTest {

    private static final Long PROJECT_ID = 99001L;
    private static final Long USER_ID = 99001L;
    private static final Long ASSIGNEE_ID = 99002L;

    @Autowired
    private GanttUpdateTaskService ganttUpdateTaskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestAuthenticatedUserProvider authenticatedUserProvider;

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

        ganttUpdateTaskService.updateTask(PROJECT_ID, 99001L, request);

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
                () -> ganttUpdateTaskService.updateTask(PROJECT_ID, 99001L, request)
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
                () -> ganttUpdateTaskService.updateTask(PROJECT_ID, 99001L, request)
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
                () -> ganttUpdateTaskService.updateTask(PROJECT_ID, 99001L, request)
        );

        assertThat(exception.getCode()).isEqualTo("BAD_REQUEST");
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

        ganttUpdateTaskService.updateTask(PROJECT_ID, 99001L, request);

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

        ganttUpdateTaskService.updateTask(PROJECT_ID, 99001L, request);

        Task task = taskRepository.findById(99001L).orElseThrow();
        assertThat(task.getTaskStatus()).isEqualTo(Task.TaskStatus.DONE);
        assertThat(task.getIsCompleted()).isTrue();
    }

    @Test
    @DisplayName("태스크 담당자가 아니면 상태 변경이 거부된다.")
    void updateTask_whenNotAssignee_throwsForbidden() {
        authenticatedUserProvider.setRole("USER");
        authenticatedUserProvider.setUserId(USER_ID);

        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttUpdateTaskService.updateTask(PROJECT_ID, 99001L, new UpdateTaskRequest(
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
                () -> ganttUpdateTaskService.updateTask(PROJECT_ID, 99001L, new UpdateTaskRequest(
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
    @DisplayName("PM도 태스크 상태 변경이 가능하다.")
    void updateTask_whenPmChangesStatus_updatesStatus() {
        authenticatedUserProvider.setRole("PM");

        ganttUpdateTaskService.updateTask(PROJECT_ID, 99001L, new UpdateTaskRequest(
                null,
                null,
                null,
                null,
                null,
                Task.TaskStatus.INPROGRESS,
                null,
                null
        ));

        Task task = taskRepository.findById(99001L).orElseThrow();
        assertThat(task.getTaskStatus()).isEqualTo(Task.TaskStatus.INPROGRESS);
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
