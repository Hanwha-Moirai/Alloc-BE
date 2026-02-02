package com.moirai.alloc.management.controllerLayerTest;

import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.userAssign.MyPendingAssignmentDTO;
import com.moirai.alloc.management.query.service.GetMyPendingAssignments;
import com.moirai.alloc.project.command.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class GetMyPendingAssignmentsTest {
    @Mock
    SquadAssignmentRepository assignmentRepository;

    @Mock
    ProjectRepository projectRepository;

    @InjectMocks
    GetMyPendingAssignments getMyPendingAssignments;

    @Test
    void should_return_pending_assignments_for_user() {
        // given
        Long userId = 1L;
        Long projectId = 100L;

        SquadAssignment assignment =
                SquadAssignment.propose(projectId, userId, 80);

        Project project = mock(Project.class);
        given(project.getName()).willReturn("AI Allocation Project");

        given(assignmentRepository.findByUserIdAndAssignmentStatus(
                userId,
                AssignmentStatus.REQUESTED
        )).willReturn(List.of(assignment));

        given(projectRepository.findById(projectId))
                .willReturn(Optional.of(project));

        // when
        List<MyPendingAssignmentDTO> result =
                getMyPendingAssignments.getMyPendingAssignments(userId);

        // then
        assertThat(result).hasSize(1);

        MyPendingAssignmentDTO dto = result.get(0);
        assertThat(dto.getProjectId()).isEqualTo(projectId);

        assertThat(dto.getProjectName()).isEqualTo("AI Allocation Project");
        assertThat(dto.getStatus()).isEqualTo(AssignmentStatus.REQUESTED);
    }
    @Test
    void should_return_empty_list_when_no_pending_assignments() {
        // given
        Long userId = 1L;

        given(assignmentRepository.findByUserIdAndAssignmentStatus(
                userId,
                AssignmentStatus.REQUESTED
        )).willReturn(List.of());

        // when
        List<MyPendingAssignmentDTO> result =
                getMyPendingAssignments.getMyPendingAssignments(userId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void should_only_query_requested_status() {
        // given
        Long userId = 1L;

        // when
        getMyPendingAssignments.getMyPendingAssignments(userId);

        // then
        then(assignmentRepository).should()
                .findByUserIdAndAssignmentStatus(
                        userId,
                        AssignmentStatus.REQUESTED
                );
    }




}
