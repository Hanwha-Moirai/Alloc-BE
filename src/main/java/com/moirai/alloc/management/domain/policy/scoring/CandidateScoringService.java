package com.moirai.alloc.management.domain.policy.scoring;

import com.moirai.alloc.management.domain.policy.scoring.calculator.availability.AvailabilityFitCalculator;
import com.moirai.alloc.management.domain.policy.scoring.calculator.experience.ExperienceFitCalculator;
import com.moirai.alloc.management.domain.policy.scoring.calculator.role.RoleFitCalculator;
import com.moirai.alloc.management.domain.policy.scoring.calculator.skill.SkillFitCalculator;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CandidateScoringService {

    private final SkillFitCalculator skillFit;
    private final ExperienceFitCalculator experienceFit;
    private final AvailabilityFitCalculator availabilityFit;
    private final RoleFitCalculator roleFit;

    public CandidateScore score(Project project, Employee employee) {

        return CandidateScore.builder()
                .userId(employee.getUserId())
                .skillScore(skillFit.calculate(project, employee))
                .experienceScore(experienceFit.calculate(project, employee))
                .availabilityScore(availabilityFit.calculate(project, employee))
                .roleScore(roleFit.calculate(project, employee))
                .build();
    }
}
// command service 와, query service에서 호출하도록 설계
// 직원 1명의 원 점수 계산기