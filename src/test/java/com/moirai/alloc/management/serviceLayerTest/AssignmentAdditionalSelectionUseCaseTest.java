package com.moirai.alloc.management.serviceLayerTest;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.command.service.SelectAdditionalAssignmentCandidates;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.AssignmentShortageCalculator;
import com.moirai.alloc.management.domain.policy.CandidateSelectionService;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.service.GetAssignedStatus;
import com.moirai.alloc.project.command.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
//시나리오: 이미 일부 인원이 ASSIGNED된 상태에서 프로젝트 요구 인원보다 부족한 수만큼만 추가 후보가 선발된다
@ExtendWith(MockitoExtension.class)
class AssignmentAdditionalSelectionUseCaseTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SquadAssignmentRepository assignmentRepository;
    @Mock
    private GetAssignedStatus getAssignedStatus;
    @Mock
    private AssignmentShortageCalculator shortageCalculator;

    @Mock
    private CandidateSelectionService candidateSelectionService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SelectAdditionalAssignmentCandidates useCase;

    @Test
    void selectsOnlyMissingNumberOfCandidatesWhenShortageExists() {
        // given
        Long projectId = 1L;

        Project project = mock(Project.class);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        //실제 코드에서 호출되는 메서드만
        when(shortageCalculator.calculate(project))
                .thenReturn(Map.of(10L, 1));

        AssignCandidateDTO dto =
                new AssignCandidateDTO(
                        projectId,
                        List.of(
                                new JobAssignmentDTO(
                                        10L,
                                        List.of(
                                                new ScoredCandidateDTO(100L, 90)
                                        )
                                )
                        )
                );

        when(candidateSelectionService.select(project, Map.of(10L, 1)))
                .thenReturn(dto);
        when(assignmentRepository.save(any(SquadAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        useCase.selectAdditionalCandidates(projectId);

        // then
        verify(assignmentRepository, times(1))
                .save(any(SquadAssignment.class));
    }
}
