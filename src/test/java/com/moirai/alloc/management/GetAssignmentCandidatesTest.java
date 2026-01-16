package com.moirai.alloc.management;

import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.domain.TitleStandard;
import com.moirai.alloc.hr.command.repository.JobStandardRepository;
import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.policy.CandidateSelectionService;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidatesView;
import com.moirai.alloc.management.query.service.GetAssignmentCandidates;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.project.command.domain.Project;
import com.moirai.alloc.user.command.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// 시나리오: 프로젝트 등록하여 JobRequirement 1개 존재, 선발된 인원은 없고,
// candidates 리스트에 추천 후보 1명 포함해서 반환하는지 테스트
@ExtendWith(MockitoExtension.class)
class GetAssignmentCandidatesTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SquadAssignmentRepository assignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JobStandardRepository jobStandardRepository;

    @Mock
    private CandidateSelectionService candidateSelectionService;

    @InjectMocks
    private GetAssignmentCandidates getAssignmentCandidates;

    @Test
    void includesRecommendedCandidatesEvenIfNotAssignedYet() {
        // given
        Long projectId = 1L;
        Long jobId = 10L;
        Long userId = 100L;

        Project project = mock(Project.class);
        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(project.getJobRequirements())
                .thenReturn(List.of(new JobRequirement(jobId, 1)));

        // 추천 후보 (policy 결과)
        AssignCandidateDTO recommended =
                new AssignCandidateDTO(
                        projectId,
                        List.of(
                                new JobAssignmentDTO(
                                        jobId,
                                        List.of(new ScoredCandidateDTO(userId, 90))
                                )
                        )
                );

        when(candidateSelectionService.select(any(), any()))
                .thenReturn(recommended);

        // 아직 선발된 인원 없음
        when(assignmentRepository.findByProjectId(projectId))
                .thenReturn(List.of());

        // Employee 존재
        Employee employee = mock(Employee.class);
        User user = mock(User.class);
        JobStandard job = mock(JobStandard.class);
        TitleStandard title = mock(TitleStandard.class);

        when(employee.getUserId()).thenReturn(userId);
        when(employee.getUser()).thenReturn(user);
        when(employee.getJob()).thenReturn(job);
        when(employee.getTitleStandard()).thenReturn(title);

        when(user.getUserId()).thenReturn(userId);
        when(user.getUserName()).thenReturn("candidate");

        when(job.getJobId()).thenReturn(jobId);
        when(job.getJobName()).thenReturn("Backend");

        when(title.getMonthlyCost()).thenReturn(500);

        when(employee.getSkills()).thenReturn(List.of());

        when(employeeRepository.findAllById(List.of(userId)))
                .thenReturn(List.of(employee));

        when(jobStandardRepository.findAllById(List.of(jobId)))
                .thenReturn(List.of(job));

        when(assignmentRepository.findUserIdsByFinalDecision(FinalDecision.ASSIGNED))
                .thenReturn(Set.of());

        // when
        AssignmentCandidatesView view =
                getAssignmentCandidates.getAssignmentCandidates(projectId);

        // then
        assertThat(view.getCandidates()).hasSize(1);

        AssignmentCandidateItemDTO candidate =
                view.getCandidates().get(0);

        assertThat(candidate.getUserId()).isEqualTo(userId);
        assertThat(candidate.getJobName()).isEqualTo("Backend");
        assertThat(candidate.getFitnessScore()).isEqualTo(90);
    }
}
