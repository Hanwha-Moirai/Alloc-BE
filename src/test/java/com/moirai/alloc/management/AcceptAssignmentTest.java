package com.moirai.alloc.management;

import com.moirai.alloc.management.command.service.AcceptAssignment;
import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AcceptAssignmentTest {

    @Mock
    private SquadAssignmentRepository assignmentRepository;

    @InjectMocks
    private AcceptAssignment acceptAssignment;

    @Test
    void assigned_user_can_accept_assignment() {
        // given
        SquadAssignment assignment =
                SquadAssignment.propose(1L, 10L, 80);

        when(assignmentRepository.findById(1L))
                .thenReturn(Optional.of(assignment));

        // when
        acceptAssignment.acceptAssignment(1L, 10L);

        // then
        assertThat(assignment.getAssignmentStatus())
                .isEqualTo(AssignmentStatus.ACCEPTED);
    }

    @Test
    void non_assigned_user_cannot_accept_assignment() {
        SquadAssignment assignment =
                SquadAssignment.propose(1L, 10L, 80);

        when(assignmentRepository.findById(1L))
                .thenReturn(Optional.of(assignment));

        assertThatThrownBy(() ->
                acceptAssignment.acceptAssignment(1L, 99L)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
