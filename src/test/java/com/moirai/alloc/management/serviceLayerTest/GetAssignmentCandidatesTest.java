package com.moirai.alloc.management.serviceLayerTest;

import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.domain.TitleStandard;
import com.moirai.alloc.hr.command.repository.JobStandardRepository;
import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.CandidateSelectionService;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScore;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScoringService;
import com.moirai.alloc.management.domain.policy.scoring.ScoreWeight;
import com.moirai.alloc.management.domain.policy.scoring.WeightPolicy;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.candidateList.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentCandidatePageView;
import com.moirai.alloc.management.query.policy.ScoreWeightAdjuster;
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
@ExtendWith(MockitoExtension.class)
class GetAssignmentCandidatesTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private SquadAssignmentRepository assignmentRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private JobStandardRepository jobStandardRepository;
    @Mock private CandidateSelectionService candidateSelectionService;

    @Mock private WeightPolicy weightPolicy;
    @Mock private ScoreWeightAdjuster scoreWeightAdjuster;
    @Mock private CandidateScoringService candidateScoringService;

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

        when(assignmentRepository.findByProjectId(projectId))
                .thenReturn(List.of());

        when(assignmentRepository.findUserIdsByFinalDecision(FinalDecision.ASSIGNED))
                .thenReturn(Set.of());

        // ✅ scoring 관련 stub (핵심)
        when(candidateScoringService.score(any(), any()))
                .thenReturn(
                        CandidateScore.builder()
                                .userId(userId)
                                .skillScore(30)
                                .experienceScore(30)
                                .availabilityScore(30)
                                .build()
                );

        when(weightPolicy.getBaseWeight(any()))
                .thenReturn(new ScoreWeight(1.0, 1.0, 1.0));

        when(scoreWeightAdjuster.adjust(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(weightPolicy.apply(any(CandidateScore.class), any()))
                .thenReturn(90);

        Employee employee = mock(Employee.class);
        User user = mock(User.class);
        JobStandard job = mock(JobStandard.class);
        TitleStandard title = mock(TitleStandard.class);

        when(employee.getUserId()).thenReturn(userId);
        when(employee.getUser()).thenReturn(user);
        when(employee.getJob()).thenReturn(job);
        when(employee.getTitleStandard()).thenReturn(title);
        when(employee.getSkills()).thenReturn(List.of());

        when(user.getUserId()).thenReturn(userId);
        when(user.getUserName()).thenReturn("candidate");

        when(job.getJobId()).thenReturn(jobId);
        when(job.getJobName()).thenReturn("Backend");

        when(title.getMonthlyCost()).thenReturn(500);

        when(employeeRepository.findAllById(any()))
                .thenReturn(List.of(employee));

        when(jobStandardRepository.findAllById(List.of(jobId)))
                .thenReturn(List.of(job));

        // when
        AssignmentCandidatePageView view =
                getAssignmentCandidates.getAssignmentCandidates(projectId, null);

        // then
        assertThat(view.getCandidates()).hasSize(1);

        AssignmentCandidateItemDTO candidate =
                view.getCandidates().get(0);

        assertThat(candidate.getUserId()).isEqualTo(userId);
        assertThat(candidate.getJobName()).isEqualTo("Backend");
        assertThat(candidate.getSkillScore()).isGreaterThanOrEqualTo(0);
        assertThat(candidate.getExperienceScore()).isGreaterThanOrEqualTo(0);
        assertThat(candidate.getAvailabilityScore()).isGreaterThanOrEqualTo(0);
        assertThat(candidate.isSelected()).isFalse();
    }

}
