package com.moirai.alloc.management.domain.policy.calculator.skill;

import com.moirai.alloc.management.domain.vo.TechRequirement;
import com.moirai.alloc.profile.common.domain.Employee;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Component;

@Component
public class SkillFitCalculator {
    public int calculate(Project project, Employee employee) {
        // 평균 × 충족률
        int total = project.getTechRequirements().size();
        if (total == 0) {
            return 0;
        }

        int satisfied = 0;
        double sum = 0.0;

        for (TechRequirement req : project.getTechRequirements()) {

            int ownedLevel = employee.getSkillLevel(req.getTechId());
            int requiredLevel = req.getTechLevel().ordinal() + 1;

            // (1) 기술별 점수
            double skillScore =
                    Math.min((double) ownedLevel / requiredLevel, 1.0);

            sum += skillScore;

            // (2) 충족 여부
            if (skillScore >= 1.0) {
                satisfied++;
            }
        }

        // (3) 평균 × 충족률
        return (int) Math.floor((sum / total) * ((double) satisfied / total) * 100);
    }
}
