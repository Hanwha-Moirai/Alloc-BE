package com.moirai.alloc.notification.gantt;

import com.moirai.alloc.gantt.command.application.dto.request.CreateMilestoneRequest;
import com.moirai.alloc.gantt.command.application.dto.request.CreateTaskRequest;
import com.moirai.alloc.gantt.command.application.dto.request.UpdateTaskRequest;
import com.moirai.alloc.gantt.command.application.service.GanttCommandService;
import com.moirai.alloc.gantt.command.application.service.GanttUpdateTaskService;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import com.moirai.alloc.notification.common.contract.AlarmTemplateType;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.TargetType;
import com.moirai.alloc.notification.common.port.NotificationPort;
import com.moirai.alloc.report.command.service.WeeklyReportCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("local")
class NotificationTest {

    @Autowired
    private GanttCommandService ganttCommandService;

    @Autowired
    private GanttUpdateTaskService ganttUpdateTaskService;

    @Autowired
    private WeeklyReportCommandService weeklyReportCommandService;

    @MockitoBean
    private NotificationPort notificationPort;

    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Test
    @DisplayName("태스크 생성 시 담당자에게 TASK_ASSIGN 알림을 보낸다.")
    @Sql(scripts = "/sql/gantt/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/gantt/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void createTask_sendsTaskAssignNotification() {
        CreateTaskRequest request = new CreateTaskRequest(
                99001L,
                99002L,
                Task.TaskCategory.DEVELOPMENT,
                "NOTIFY_TASK_99001",
                "desc",
                LocalDate.of(2025, 1, 3),
                LocalDate.of(2025, 1, 4)
        );

        Long taskId = ganttCommandService.createTask(99001L, request);

        ArgumentCaptor<InternalNotificationCommand> captor =
                ArgumentCaptor.forClass(InternalNotificationCommand.class);
        verify(notificationPort).notify(captor.capture());
        InternalNotificationCommand command = captor.getValue();

        assertThat(command.templateType()).isEqualTo(AlarmTemplateType.TASK_ASSIGN);
        assertThat(command.targetType()).isEqualTo(TargetType.TASK);
        assertThat(command.targetId()).isEqualTo(taskId);
        assertThat(command.targetUserIds()).containsExactly(99002L);
        assertThat(command.variables()).isEqualTo(Map.of("taskName", "NOTIFY_TASK_99001"));
        assertThat(command.linkUrl()).isEqualTo("/projects/99001/tasks/" + taskId);
    }

    @Test
    @DisplayName("담당자 변경 시 새 담당자에게 TASK_ASSIGN 알림을 보낸다.")
    @Sql(scripts = "/sql/gantt/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/gantt/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void updateTask_assigneeChange_sendsTaskAssignNotification() {
        when(authenticatedUserProvider.getCurrentUserRole()).thenReturn("PM");
        clearInvocations(notificationPort);

        UpdateTaskRequest request = new UpdateTaskRequest(
                null,
                99001L,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ganttUpdateTaskService.updateTask(99001L, 99001L, request);

        ArgumentCaptor<InternalNotificationCommand> captor =
                ArgumentCaptor.forClass(InternalNotificationCommand.class);
        verify(notificationPort).notify(captor.capture());
        InternalNotificationCommand command = captor.getValue();

        assertThat(command.templateType()).isEqualTo(AlarmTemplateType.TASK_ASSIGN);
        assertThat(command.targetType()).isEqualTo(TargetType.TASK);
        assertThat(command.targetId()).isEqualTo(99001L);
        assertThat(command.targetUserIds()).containsExactly(99001L);
        assertThat(command.variables()).containsEntry("taskName", "Seed Task 1");
        assertThat(command.linkUrl()).isEqualTo("/projects/99001/tasks/99001");
    }

    @Test
    @DisplayName("마일스톤 생성 시 프로젝트 참여자에게 MILESTONE 알림을 보낸다.")
    @Sql(scripts = "/sql/gantt/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/gantt/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void createMilestone_sendsMilestoneNotification() {
        CreateMilestoneRequest request = new CreateMilestoneRequest(
                "NOTIFY_MILESTONE",
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 12),
                0L
        );

        Long milestoneId = ganttCommandService.createMilestone(99001L, request);

        ArgumentCaptor<InternalNotificationCommand> captor =
                ArgumentCaptor.forClass(InternalNotificationCommand.class);
        verify(notificationPort).notify(captor.capture());
        InternalNotificationCommand command = captor.getValue();

        assertThat(command.templateType()).isEqualTo(AlarmTemplateType.MILESTONE);
        assertThat(command.targetType()).isEqualTo(TargetType.MILESTONE);
        assertThat(command.targetId()).isEqualTo(milestoneId);
        assertThat(command.targetUserIds()).containsExactlyInAnyOrder(99001L, 99002L);
        assertThat(command.variables()).isEqualTo(Map.of("milestoneName", "NOTIFY_MILESTONE"));
        assertThat(command.linkUrl()).isEqualTo("/projects/99001/gantt");
    }
}
