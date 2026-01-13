package com.moirai.alloc.management.domain.policy.calculator.experience.fit;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.profile.common.domain.Employee;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectTypeExperiencePolicy {
    // 동일한 프로젝트 유형을 assigned 상태로 몇번 해보았는지
    private static final int SCORE_PER_PROJECT = 10;
    private static final int MAX_SCORE = 40;

    private final SquadAssignmentRepository assignmentRepository;

    public int score(Project project, Employee employee) {

        long sameTypeCount =
                assignmentRepository.countAssignedProjectsByType(
                        employee.getUserId(),
                        project.getProjectType()
                );

        return (int) Math.min(sameTypeCount * SCORE_PER_PROJECT, MAX_SCORE);
    }
}
