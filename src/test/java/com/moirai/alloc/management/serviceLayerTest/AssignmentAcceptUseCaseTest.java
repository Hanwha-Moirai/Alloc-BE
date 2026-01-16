package com.moirai.alloc.management.serviceLayerTest;

import com.moirai.alloc.management.command.service.AcceptAssignment;
import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

//직원이 선발되고 → ACCEPT → 자동 ASSIGNED
@ExtendWith(MockitoExtension.class)
class AssignmentAcceptUseCaseTest {

    @Mock
    private SquadAssignmentRepository assignmentRepository;

    @InjectMocks
    private AcceptAssignment acceptAssignment;

    @Test
    void employeeAcceptsAssignmentAndIsAutomaticallyAssigned() {
        // given: 선발된 상태 (REQUESTED, FINAL=PENDING)
        SquadAssignment assignment =
                SquadAssignment.propose(1L, 10L, 80);

        when(assignmentRepository.findById(1L))
                .thenReturn(Optional.of(assignment));

        // when: 직원이 수락
        acceptAssignment.acceptAssignment(1L, 10L);

        // then: 자동 ASSIGNED
        assertThat(assignment.getAssignmentStatus())
                .isEqualTo(AssignmentStatus.ACCEPTED);
        assertThat(assignment.getFinalDecision())
                .isEqualTo(FinalDecision.ASSIGNED);
    }

    @Test
    void otherUserCannotAcceptAssignment() {
        // given
        SquadAssignment assignment =
                SquadAssignment.propose(1L, 10L, 80);

        when(assignmentRepository.findById(1L))
                .thenReturn(Optional.of(assignment));

        // when / then
        assertThatThrownBy(() ->
                acceptAssignment.acceptAssignment(1L, 99L)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
