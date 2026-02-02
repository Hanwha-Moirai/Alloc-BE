package com.moirai.alloc.management.query.service;

import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScore;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScoringService;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.selectedList.SelectedAssignmentMemberDTO;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAssignedMembers {
//프로젝트에 배정 절차가 시작된 사람들
    private final SquadAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final CandidateScoringService candidateScoringService;
    private final ProjectRepository projectRepository;

    public List<SelectedAssignmentMemberDTO> getMembers(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        List<SquadAssignment> assignments =
                assignmentRepository.findByProjectId(projectId);

        List<Long> userIds = assignments.stream()
                .map(SquadAssignment::getUserId)
                .toList();

        Map<Long, Employee> employeeMap =
                employeeRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(Employee::getUserId, e -> e));

        return assignments.stream()
                .map(a -> {
                    Employee e = employeeMap.get(a.getUserId());

                    CandidateScore score =
                            candidateScoringService.score(project, e);
                    int finalScore =
                            (score.getSkillScore()
                                    + score.getExperienceScore()
                                    + score.getAvailabilityScore());

                    return new SelectedAssignmentMemberDTO(
                            a.getAssignmentId(),
                            a.getUserId(),
                            e.getUser().getUserName(),
                            e.getJob().getJobName(),
                            resolveMainSkill(e),
                            e.getTitleStandard().getMonthlyCost(),
                            a.getAssignmentStatus(),             // 직원 응답 상태
                            a.getFinalDecision(),                // 최종 결정
                            score.getSkillScore(),
                            score.getExperienceScore(),
                            score.getAvailabilityScore(),
                            finalScore
                    );
                })
                .toList();
    }

    private String resolveMainSkill(Employee employee) {
        return employee.getSkills().stream()
                .max(Comparator.comparingInt(
                        skill -> skill.getProficiency().ordinal()
                ))
                .map(skill -> skill.getTech().getTechName())
                .orElse(null);
    }
}
