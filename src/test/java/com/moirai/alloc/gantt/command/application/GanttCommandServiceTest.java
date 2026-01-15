package com.moirai.alloc.gantt.command.application;

import com.moirai.alloc.gantt.command.application.dto.request.CompleteTaskRequest;
import com.moirai.alloc.gantt.command.application.dto.request.CreateTaskRequest;
import com.moirai.alloc.gantt.command.application.service.GanttCommandService;
import com.moirai.alloc.gantt.command.domain.entity.Task;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("local")
@Sql(scripts = "/sql/gantt/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
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
    private TaskUpdateLogRepository taskUpdateLogRepository;

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
    @DisplayName("태스크 상태 변경 권한이 없습니다.")
    void completeTask_whenAlreadyDone_throwsConflict() {
        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.completeTask(PROJECT_ID, 99002L, new CompleteTaskRequest("done"))
        );
        assertThat(exception.getCode()).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("PM 권한이 없으면 태스크 삭제 권한 불허")
    void deleteMilestone_whenHasTasks_throwsConflict() {
        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.deleteMilestone(PROJECT_ID, MILESTONE_ID)
        );

        assertThat(exception.getCode()).isEqualTo("FORBIDDEN");
    }

    @TestConfiguration
    static class TestAuthConfig {
        @Bean
        @Primary
        AuthenticatedUserProvider authenticatedUserProvider() {
            return () -> USER_ID;
        }
    }
}
