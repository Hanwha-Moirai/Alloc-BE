package com.moirai.alloc.management.domain.policy.calculator.experience;

import com.moirai.alloc.management.domain.policy.calculator.experience.fit.ProjectScaleExperiencePolicy;
import com.moirai.alloc.management.domain.policy.calculator.experience.fit.ProjectTypeExperiencePolicy;
import com.moirai.alloc.management.domain.policy.calculator.experience.fit.RecencyExperiencePolicy;
import com.moirai.alloc.management.domain.policy.calculator.experience.fit.TechExperiencePolicy;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.profile.common.domain.Employee;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExperienceFitCalculator {

    private final ProjectTypeExperiencePolicy projectTypeExperiencePolicy;
    private final TechExperiencePolicy techExperiencePolicy;
    private final RecencyExperiencePolicy recencyExperiencePolicy;
    private final ProjectScaleExperiencePolicy projectScaleExperiencePolicy;
    private final SquadAssignmentRepository assignmentRepository;

    //경험 적합도 계산 (0 ~ 100)

    public int calculate(Project project, Employee employee) {

        int typeScore =
                projectTypeExperiencePolicy.score(project, employee); // 0 ~ 40

//        int techScore =
//                techExperiencePolicy.score(project, employee);         // 0 ~ 10

        int recencyScore =
                recencyExperiencePolicy.score(project, employee);      // 0 ~ 20

        int scaleScore =
                projectScaleExperiencePolicy.calculate(project, employee); // 0 ~ 30
        int totalScore = typeScore + recencyScore + scaleScore;
//        int totalScore = typeScore + techScore + recencyScore + scaleScore;

        // (5) 신입 보호 최소 점수 하한 보정
//        boolean hasAssignedExperience =
//                assignmentRepository.existsByUserIdAndFinalDecision(
//                        employee.getUserId(),
//                        FinalDecision.ASSIGNED
//                );
//
//        if (!hasAssignedExperience) {
//            totalScore = Math.max(totalScore, 20);
//        }

        return totalScore;
    }
}
