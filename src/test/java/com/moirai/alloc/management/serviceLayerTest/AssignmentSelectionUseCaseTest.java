package com.moirai.alloc.management.serviceLayerTest;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.command.event.ProjectTempAssignmentEvent;
import com.moirai.alloc.management.command.service.SelectAssignmentCandidates;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.project.command.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//프로젝트 요구사항에 맞게 후보 선발
@ExtendWith(MockitoExtension.class)
class AssignmentSelectionUseCaseTest {

    @Mock
    private SquadAssignmentRepository assignmentRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SelectAssignmentCandidates selectAssignmentCandidates;

    @Test
    void candidatesAreSelectedWhenRequiredCountIsSatisfied() {
        // given
        Long projectId = 1L;
        Long jobId = 10L;

        Project project = mock(Project.class);
        when(project.getProjectId()).thenReturn(projectId);
        when(project.getJobRequirements())
                .thenReturn(List.of(new JobRequirement(jobId, 2)));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(assignmentRepository.existsByProjectIdAndUserId(anyLong(), anyLong()))
                .thenReturn(false);

        // save()가 null 반환하면 NPE 나므로 그대로 반환하게 설정
        when(assignmentRepository.save(any(SquadAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssignCandidateDTO command = new AssignCandidateDTO(
                projectId,
                List.of(
                        new JobAssignmentDTO(
                                jobId,
                                List.of(
                                        new ScoredCandidateDTO(100L, 80),
                                        new ScoredCandidateDTO(101L, 70)
                                )
                        )
                )
        );

        // when
        selectAssignmentCandidates.selectAssignmentCandidates(command);

        // then
        verify(assignmentRepository, times(2))
                .save(any(SquadAssignment.class));

        verify(eventPublisher, times(2))
                .publishEvent(any(ProjectTempAssignmentEvent.class));
    }

    @Test
    void selectionFailsWhenRequiredCountIsNotMet() {
        // given
        Long projectId = 1L;
        Long jobId = 10L;

        Project project = mock(Project.class);
        when(project.getJobRequirements())
                .thenReturn(List.of(new JobRequirement(jobId, 2)));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        AssignCandidateDTO command = new AssignCandidateDTO(
                projectId,
                List.of(
                        new JobAssignmentDTO(
                                jobId,
                                List.of(new ScoredCandidateDTO(100L, 80))
                        )
                )
        );

        // when / then
        assertThatThrownBy(() ->
                selectAssignmentCandidates.selectAssignmentCandidates(command)
        ).isInstanceOf(IllegalArgumentException.class);

        verify(assignmentRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}