package com.moirai.alloc.management;

import com.moirai.alloc.management.command.service.DecideFinalAssignment;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecideFinalAssignmentTest {

    @Mock
    private SquadAssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DecideFinalAssignment decideFinalAssignment;

    @Test
    void pm_can_make_final_decision() {
        SquadAssignment assignment =
                SquadAssignment.propose(1L, 10L, 90);

        User pm = User.builder()
                .loginId("pm_login")
                .password("password")
                .userName("pm")
                .email("pm@test.com")
                .phone("010-0000-0000")
                .auth(User.Auth.PM)
                .build();

        ReflectionTestUtils.setField(pm, "userId", 1L);

        when(assignmentRepository.findById(1L))
                .thenReturn(Optional.of(assignment));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(pm));

        decideFinalAssignment.decideFinalAssignment(
                1L, 1L, FinalDecision.ASSIGNED
        );

        assertThat(assignment.getFinalDecision())
                .isEqualTo(FinalDecision.ASSIGNED);
    }

    @Test
    void non_pm_cannot_make_final_decision() {
        SquadAssignment assignment =
                SquadAssignment.propose(1L, 10L, 90);

        User user = User.builder()
                .loginId("user_login")
                .password("password")
                .userName("user")
                .email("user@test.com")
                .phone("010-1111-1111")
                .auth(User.Auth.USER)
                .build();

        ReflectionTestUtils.setField(user, "userId", 2L);

        when(assignmentRepository.findById(1L))
                .thenReturn(Optional.of(assignment));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                decideFinalAssignment.decideFinalAssignment(
                        1L, 2L, FinalDecision.ASSIGNED
                )
        ).isInstanceOf(IllegalStateException.class);
    }
}
