package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.command.event.ProjectFinalAssignmentEvent;
import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.project.command.domain.Project;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//INTERVIEW 요청 → PM이 EXCLUDED
@ExtendWith(MockitoExtension.class)
class AssignmentInterviewFlowUseCaseTest {

    @Mock
    private SquadAssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DecideFinalAssignment decideFinalAssignment;

    // =========================
    // ASSIGNED 케이스
    // =========================

    //pm이 assign하면 상태변경 + 이벤트 발행 여부
    @Test
    void pmAssignsCandidate_success() {

        Long assignmentId = 1L;
        Long projectId = 100L;
        Long userId = 10L;
        Long pmId = 1L;

        SquadAssignment assignment = SquadAssignment.propose(projectId, userId, 80);
        assignment.requestInterview(userId);

        User pm = pmUser(pmId);

        Project project = mock(Project.class);
        when(project.getName()).thenReturn("Alloc Project");

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        when(userRepository.findById(pmId))
                .thenReturn(Optional.of(pm));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        // when
        decideFinalAssignment.decideFinalAssignment(
                assignmentId,
                pmId,
                FinalDecision.ASSIGNED
        );

        // then
        assertThat(assignment.getFinalDecision())
                .isEqualTo(FinalDecision.ASSIGNED);

        verify(eventPublisher, times(1))
                .publishEvent(any(ProjectFinalAssignmentEvent.class));
    }

    // =========================
    // EXCLUDED 케이스
    // =========================
    //pm이 excluded 하면 상태만 바뀌고 이벤트 안 발생하는지
    @Test
    void pmExcludesCandidate_success() {

        Long assignmentId = 1L;
        Long projectId = 100L;
        Long userId = 10L;
        Long pmId = 1L;

        SquadAssignment assignment = SquadAssignment.propose(projectId, userId, 80);
        assignment.requestInterview(userId);

        User pm = pmUser(pmId);

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        when(userRepository.findById(pmId))
                .thenReturn(Optional.of(pm));

        // when
        decideFinalAssignment.decideFinalAssignment(
                assignmentId,
                pmId,
                FinalDecision.EXCLUDED
        );

        // then
        assertThat(assignment.getFinalDecision())
                .isEqualTo(FinalDecision.EXCLUDED);

        verify(eventPublisher, never()).publishEvent(any());
    }

    // =========================
    // PM 권한 아님
    // =========================
    //pm이 아닌 유저는 최종 결정 불가
    @Test
    void nonPmUserCannotDecide() {

        Long assignmentId = 1L;
        Long projectId = 100L;
        Long userId = 10L;
        Long userNotPmId = 2L;

        SquadAssignment assignment = SquadAssignment.propose(projectId, userId, 80);
        assignment.requestInterview(userId);

        User normalUser = normalUser(userNotPmId);

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        when(userRepository.findById(userNotPmId))
                .thenReturn(Optional.of(normalUser));

        assertThatThrownBy(() ->
                decideFinalAssignment.decideFinalAssignment(
                        assignmentId,
                        userNotPmId,
                        FinalDecision.ASSIGNED
                )
        ).isInstanceOf(IllegalStateException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    // =========================
    // Assignment 없음
    // =========================
    @Test
    void assignmentNotFoundThrowException() {

        when(assignmentRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                decideFinalAssignment.decideFinalAssignment(
                        1L,
                        1L,
                        FinalDecision.ASSIGNED
                )
        ).isInstanceOf(RuntimeException.class);
    }

    // =========================
    // User 없음
    // =========================
    @Test
    void userNotFoundThrowException() {

        Long assignmentId = 1L;
        Long projectId = 100L;
        Long userId = 10L;

        SquadAssignment assignment = SquadAssignment.propose(projectId, userId, 80);
        assignment.requestInterview(userId);

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                decideFinalAssignment.decideFinalAssignment(
                        assignmentId,
                        1L,
                        FinalDecision.ASSIGNED
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    // =========================
    // Fixture
    // =========================

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

    private User normalUser(Long id) {
        User user = User.builder()
                .loginId("user")
                .password("pw")
                .userName("USER")
                .email("user@test.com")
                .phone("010")
                .auth(User.Auth.USER)
                .build();

        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }
}
