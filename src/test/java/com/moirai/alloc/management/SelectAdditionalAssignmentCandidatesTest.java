package com.moirai.alloc.management;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.command.service.SelectAdditionalAssignmentCandidates;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.service.CandidateSelectionService;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.select_list.AssignmentStatusDTO;
import com.moirai.alloc.management.query.service.GetSelectedAssignmentStatus;
import com.moirai.alloc.project.command.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelectAdditionalAssignmentCandidatesTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SquadAssignmentRepository assignmentRepository;

    @Mock
    private GetSelectedAssignmentStatus getAssignmentStatus;

    @Mock
    private CandidateSelectionService candidateSelectionService;

    @InjectMocks
    private SelectAdditionalAssignmentCandidates selectAdditionalAssignmentCandidates;

    @Test
    void does_nothing_when_no_shortage_exists() {
        Long projectId = 1L;

        Project project = mock(Project.class);
        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        AssignmentStatusDTO status =
                new AssignmentStatusDTO(Map.of(10L, 0));
        when(getAssignmentStatus.getStatus(projectId))
                .thenReturn(status);

        selectAdditionalAssignmentCandidates.selectAdditionalCandidates(projectId);

        verify(candidateSelectionService, never()).select(any(), any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void creates_additional_assignments_when_shortage_exists() {
        Long projectId = 1L;
        Long jobId = 10L;

        Project project = mock(Project.class);
        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        AssignmentStatusDTO status =
                new AssignmentStatusDTO(Map.of(jobId, 1));
        when(getAssignmentStatus.getStatus(projectId))
                .thenReturn(status);

        AssignCandidateDTO candidates =
                new AssignCandidateDTO(
                        projectId,
                        List.of(
                                new JobAssignmentDTO(
                                        jobId,
                                        List.of(
                                                new ScoredCandidateDTO(200L, 90),
                                                new ScoredCandidateDTO(201L, 80)
                                        )
                                )
                        )
                );

        when(candidateSelectionService.select(project, status.getShortageByJobId()))
                .thenReturn(candidates);

        when(assignmentRepository.existsByProjectIdAndUserId(any(), any()))
                .thenReturn(false);

        selectAdditionalAssignmentCandidates.selectAdditionalCandidates(projectId);

        verify(assignmentRepository, times(2))
                .save(any(SquadAssignment.class));
    }
}

