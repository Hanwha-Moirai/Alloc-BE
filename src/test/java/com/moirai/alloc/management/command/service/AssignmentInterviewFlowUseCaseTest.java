package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.domain.entity.AssignmentStatus;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

//INTERVIEW 요청 → PM이 EXCLUDED
@ExtendWith(MockitoExtension.class)
class AssignmentInterviewFlowUseCaseTest {

    @Mock
    private SquadAssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestInterview requestInterview;

    @InjectMocks
    private DecideFinalAssignment decideFinalAssignment;

    @Test
    void employeeRequestsInterviewAndPmExcludes() {
        // given: 선발 상태
        SquadAssignment assignment =
                SquadAssignment.propose(1L, 10L, 75);

        assignment.requestInterview(10L);

        User pm = pmUser(1L);

        when(assignmentRepository.findById(1L))
                .thenReturn(Optional.of(assignment));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(pm));

        // when: PM이 EXCLUDE
        decideFinalAssignment.decideFinalAssignment(
                1L, 1L, FinalDecision.EXCLUDED
        );

        // then
        assertThat(assignment.getAssignmentStatus())
                .isEqualTo(AssignmentStatus.INTERVIEW_REQUESTED);
        assertThat(assignment.getFinalDecision())
                .isEqualTo(FinalDecision.EXCLUDED);
    }

    @Test
    void employeeRequestsInterviewAndPmAssigns() {
        // given
        SquadAssignment assignment =
                SquadAssignment.propose(1L, 10L, 75);

        assignment.requestInterview(10L);

        User pm = pmUser(1L);

        when(assignmentRepository.findById(1L))
                .thenReturn(Optional.of(assignment));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(pm));

        // when
        decideFinalAssignment.decideFinalAssignment(
                1L, 1L, FinalDecision.ASSIGNED
        );

        // then
        assertThat(assignment.getFinalDecision())
                .isEqualTo(FinalDecision.ASSIGNED);
    }

    private User pmUser(Long id) {
        User pm = User.builder()
                .loginId("pm")
                .password("pw")
                .userName("PM")
                .email("pm@test.com")
                .phone("010")
                .auth(User.Auth.PM)
                .build();
        ReflectionTestUtils.setField(pm, "userId", id);
        return pm;
    }
}
