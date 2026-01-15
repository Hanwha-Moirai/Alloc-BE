package com.moirai.alloc.management.domain.policy.scoring.calculator.experience.fit;

import com.moirai.alloc.management.domain.vo.TechRequirement;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
@Component
public class TechExperiencePolicy {

    private static final int MAX_SCORE = 10;

    public int score(Project project, Employee employee) {

        List<TechRequirement> requiredTechs =
                project.getTechRequirements();

        if (requiredTechs.isEmpty()) {
            return 0;
        }

        Set<Long> experiencedTechIds =
                employee.getExperiencedTechIds();

        long matchedCount =
                requiredTechs.stream()
                        .map(TechRequirement::getTechId)
                        .filter(experiencedTechIds::contains)
                        .count();

        double ratio = (double) matchedCount / requiredTechs.size();

        return (int) Math.floor(ratio * MAX_SCORE);
    }
}

