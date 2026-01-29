package com.moirai.alloc.management.serviceLayerTest;

import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.domain.TitleStandard;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScore;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScoringService;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.selectedList.SelectedAssignmentMemberDTO;
import com.moirai.alloc.management.query.service.GetAssignedMembers;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.project.command.domain.Project;
import com.moirai.alloc.user.command.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class GetAssignedMembersTest {

    @Mock
    private SquadAssignmentRepository assignmentRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CandidateScoringService candidateScoringService;

    @InjectMocks
    private GetAssignedMembers getAssignedMembers;

    @Test
    void getMembers_returns_assigned_members() {
        // given
        Long projectId = 1L;

        Project project = mock(Project.class);
        given(projectRepository.findById(projectId))
                .willReturn(Optional.of(project));

        SquadAssignment sa1 = SquadAssignment.propose(projectId, 10L, 80);
        sa1.finalAssign();

        SquadAssignment sa2 = SquadAssignment.propose(projectId, 20L, 90);
        sa2.finalAssign();

        given(assignmentRepository.findByProjectId(projectId))
                .willReturn(List.of(sa1, sa2));

        Employee e1 = createEmployee(10L, "김철수", "Backend");
        Employee e2 = createEmployee(20L, "이영희", "Frontend");

        given(employeeRepository.findAllById(List.of(10L, 20L)))
                .willReturn(List.of(e1, e2));

        given(candidateScoringService.score(any(), any()))
                .willReturn(
                        CandidateScore.builder()
                                .skillScore(30)
                                .experienceScore(30)
                                .availabilityScore(30)
                                .build()
                );

        // when
        List<SelectedAssignmentMemberDTO> result =
                getAssignedMembers.getMembers(projectId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFinalScore()).isEqualTo(90);
        assertThat(result.get(0).getUserName()).isEqualTo("김철수");
        assertThat(result.get(1).getUserName()).isEqualTo("이영희");
    }

    /**
     * ✅ Employee 도메인 픽스처
     * - builder에 없는 필드는 ReflectionTestUtils로 세팅
     */
    private Employee createEmployee(Long userId, String userName, String jobName) {

        User user = User.builder()
                .userName(userName)
                .build();

        JobStandard job = JobStandard.builder()
                .jobName(jobName)
                .build();

        TitleStandard titleStandard = mock(TitleStandard.class);
        when(titleStandard.getMonthlyCost()).thenReturn(500);

        Employee employee = Employee.builder()
                .user(user)
                .job(job)
                .titleStandard(titleStandard)
                .build();

        // 🔑 builder에 없는 필드들 수동 주입
        ReflectionTestUtils.setField(employee, "userId", userId);
        ReflectionTestUtils.setField(employee, "skills", List.of());

        return employee;
    }
}