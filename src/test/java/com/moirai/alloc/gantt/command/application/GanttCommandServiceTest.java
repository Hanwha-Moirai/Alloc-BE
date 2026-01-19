package com.moirai.alloc.gantt.command.application;

import com.moirai.alloc.gantt.command.application.dto.request.CompleteTaskRequest;
import com.moirai.alloc.gantt.command.application.dto.request.CreateMilestoneRequest;
import com.moirai.alloc.gantt.command.application.dto.request.CreateTaskRequest;
import com.moirai.alloc.gantt.command.application.dto.request.UpdateTaskRequest;
import com.moirai.alloc.gantt.command.application.service.GanttCommandService;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import com.moirai.alloc.gantt.command.domain.repository.TaskRepository;
import com.moirai.alloc.gantt.command.domain.repository.TaskUpdateLogRepository;
import com.moirai.alloc.gantt.common.exception.GanttException;
import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import com.moirai.alloc.notification.command.domain.entity.AlarmTemplate;
import com.moirai.alloc.notification.command.domain.entity.AlarmTemplateType;
import com.moirai.alloc.notification.command.domain.entity.TargetType;
import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import com.moirai.alloc.notification.command.repository.AlarmTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
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
    private TaskUpdateLogRepository taskUpdateLogRepository;

    @Autowired
    private AlarmLogRepository alarmLogRepository;

    @Autowired
    private AlarmTemplateRepository alarmTemplateRepository;

    @BeforeEach
    void ensureTaskAssignTemplate() {
        alarmTemplateRepository
                .findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(AlarmTemplateType.TASK_ASSIGN)
                .orElseGet(() -> alarmTemplateRepository.save(
                        AlarmTemplate.builder()
                                .alarmTemplateType(AlarmTemplateType.TASK_ASSIGN)
                                .templateTitle("태스크 담당자 배정")
                                .templateContext("태스크 {{taskName}} 담당자로 지정되었습니다.")
                                .build()
                ));
        alarmTemplateRepository
                .findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(AlarmTemplateType.MILESTONE)
                .orElseGet(() -> alarmTemplateRepository.save(
                        AlarmTemplate.builder()
                                .alarmTemplateType(AlarmTemplateType.MILESTONE)
                                .templateTitle("마일스톤 생성")
                                .templateContext("마일스톤 {{milestoneName}} 이 생성되었습니다.")
                                .build()
                ));
    }

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
    @DisplayName("하위 태스크가 있으면 마일스톤 삭제가 거부된다.")
    void deleteMilestone_whenHasTasks_throwsConflict() {
        GanttException exception = assertThrows(
                GanttException.class,
                () -> ganttCommandService.deleteMilestone(PROJECT_ID, MILESTONE_ID)
        );

        assertThat(exception.getCode()).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("태스크 담당자가 변경되면 알림 로그가 생성된다.")
    void updateTask_whenAssigneeChanged_createsAlarmLog() {
        long beforeUnread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(USER_ID);

        UpdateTaskRequest request = new UpdateTaskRequest(
                null,
                USER_ID,
                null,
                "TEST_TASK_REASSIGN_99001",
                null,
                null,
                null,
                null
        );

        ganttCommandService.updateTask(PROJECT_ID, 99001L, request);

        long afterUnread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(USER_ID);
        assertThat(afterUnread).isEqualTo(beforeUnread + 1);

        var latestLog = alarmLogRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc(USER_ID, PageRequest.of(0, 1))
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(latestLog.getTargetType()).isEqualTo(TargetType.TASK);
        assertThat(latestLog.getTargetId()).isEqualTo(99001L);
        assertThat(latestLog.getLinkUrl()).isEqualTo("/projects/99001/tasks");
    }

    @Test
    @DisplayName("마일스톤 생성 시 프로젝트 멤버 모두에게 알림 로그가 생성된다.")
    void createMilestone_createsAlarmLogsForProjectMembers() {
        long beforePmUnread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(99001L);
        long beforeUserUnread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(99002L);

        Long milestoneId = ganttCommandService.createMilestone(PROJECT_ID, new CreateMilestoneRequest(
                "TEST_MILESTONE_CREATE_99001",
                LocalDate.of(2025, 1, 5),
                LocalDate.of(2025, 1, 10),
                0L
        ));

        long afterPmUnread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(99001L);
        long afterUserUnread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(99002L);

        assertThat(afterPmUnread).isEqualTo(beforePmUnread + 1);
        assertThat(afterUserUnread).isEqualTo(beforeUserUnread + 1);

        var pmLatest = alarmLogRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc(99001L, PageRequest.of(0, 1))
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(pmLatest.getTargetType()).isEqualTo(TargetType.MILESTONE);
        assertThat(pmLatest.getTargetId()).isEqualTo(milestoneId);
        assertThat(pmLatest.getLinkUrl()).isEqualTo("/projects/99001/gantt");
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
