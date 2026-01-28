package com.moirai.alloc.notification.management;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.command.event.ProjectTempAssignmentEvent;
import com.moirai.alloc.management.command.service.SelectAdditionalAssignmentCandidates;
import com.moirai.alloc.management.command.service.SelectAssignmentCandidates;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.AssignmentShortageCalculator;
import com.moirai.alloc.management.domain.policy.CandidateSelectionService;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.service.GetAssignmentStatus;
import com.moirai.alloc.notification.command.event.ManagementNotificationEventHandler;
import com.moirai.alloc.notification.common.contract.AlarmTemplateType;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.TargetType;
import com.moirai.alloc.notification.common.port.NotificationPort;
import com.moirai.alloc.project.command.domain.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTest {

    @Mock
    private NotificationPort notificationPort;

    @InjectMocks
    private ManagementNotificationEventHandler managementNotificationEventHandler;

    @Mock
    private SquadAssignmentRepository assignmentRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SelectAssignmentCandidates selectAssignmentCandidates;

    @Mock
    private GetAssignmentStatus getAssignmentStatus;

    @Mock
    private CandidateSelectionService candidateSelectionService;

    @Mock
    private AssignmentShortageCalculator shortageCalculator;

    @InjectMocks
    private SelectAdditionalAssignmentCandidates selectAdditionalAssignmentCandidates;

    @Test
    @DisplayName("프로젝트 임시 배정 이벤트가 POST_TEMP 알림으로 변환된다.")
    void projectTempAssignmentEvent_sendsNotification() {
        ProjectTempAssignmentEvent event = new ProjectTempAssignmentEvent(
                88001L,
                "PM Notification Project",
                88002L
        );

        managementNotificationEventHandler.onProjectTempAssignment(event);

        ArgumentCaptor<InternalNotificationCommand> captor =
                ArgumentCaptor.forClass(InternalNotificationCommand.class);
        verify(notificationPort).notify(captor.capture());
        InternalNotificationCommand notification = captor.getValue();

        assertThat(notification.templateType()).isEqualTo(AlarmTemplateType.POST_TEMP);
        assertThat(notification.targetType()).isEqualTo(TargetType.POST);
        assertThat(notification.targetId()).isEqualTo(88001L);
        assertThat(notification.targetUserIds()).contains(88002L);
        assertThat(notification.variables()).containsEntry("projectName", "PM Notification Project");
        assertThat(notification.linkUrl()).isEqualTo("/projects/88001/assignments");
    }

    @Test
    @DisplayName("프로젝트 임시 배정 후보 선정 시 이벤트를 발행한다.")
    void selectAssignmentCandidates_publishesTempAssignmentEvent() {
        Project project = mock(Project.class);
        when(project.getProjectId()).thenReturn(88001L);
        when(project.getName()).thenReturn("PM Notification Project");
        when(project.getJobRequirements()).thenReturn(List.of(new JobRequirement(10L, 1)));

        when(projectRepository.findById(88001L)).thenReturn(Optional.of(project));
        when(assignmentRepository.existsByProjectIdAndUserId(anyLong(), anyLong())).thenReturn(false);
        when(assignmentRepository.save(any(SquadAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AssignCandidateDTO command = new AssignCandidateDTO(
                88001L,
                List.of(
                        new JobAssignmentDTO(
                                10L,
                                List.of(new ScoredCandidateDTO(88002L, 85))
                        )
                )
        );

        selectAssignmentCandidates.selectAssignmentCandidates(command);

        ArgumentCaptor<ProjectTempAssignmentEvent> eventCaptor =
                ArgumentCaptor.forClass(ProjectTempAssignmentEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        ProjectTempAssignmentEvent event = eventCaptor.getValue();

        assertThat(event.projectId()).isEqualTo(88001L);
        assertThat(event.projectName()).isEqualTo("PM Notification Project");
        assertThat(event.userId()).isEqualTo(88002L);
    }

    @Test
    @DisplayName("추가 후보 임시 배정 시 이벤트를 발행한다.")
    void selectAdditionalAssignmentCandidates_publishesTempAssignmentEvent() {
        Project project = mock(Project.class);
        when(project.getName()).thenReturn("PM Notification Project");

        when(projectRepository.findById(88001L)).thenReturn(Optional.of(project));
        when(shortageCalculator.calculate(project)).thenReturn(Map.of(10L, 1));
        when(candidateSelectionService.select(eq(project), any()))
                .thenReturn(new AssignCandidateDTO(
                        88001L,
                        List.of(
                                new JobAssignmentDTO(
                                        10L,
                                        List.of(new ScoredCandidateDTO(88002L, 90))
                                )
                        )
                ));
        when(assignmentRepository.existsByProjectIdAndUserId(anyLong(), anyLong())).thenReturn(false);
        when(assignmentRepository.save(any(SquadAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        selectAdditionalAssignmentCandidates.selectAdditionalCandidates(88001L);

        ArgumentCaptor<ProjectTempAssignmentEvent> eventCaptor =
                ArgumentCaptor.forClass(ProjectTempAssignmentEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        ProjectTempAssignmentEvent event = eventCaptor.getValue();

        assertThat(event.projectId()).isEqualTo(88001L);
        assertThat(event.projectName()).isEqualTo("PM Notification Project");
        assertThat(event.userId()).isEqualTo(88002L);
    }
}
