package com.moirai.alloc.management.domain.policy.candidate.experience.fit;

import com.moirai.alloc.profile.common.domain.Employee;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TechExperiencePolicy {
/*
    private static final int MAX_SCORE = 10;

    private final ProjectTechRequirementRepository techRequirementRepository;

    public TechExperiencePolicy(ProjectTechRequirementRepository techRequirementRepository) {
        this.techRequirementRepository = techRequirementRepository;
    }

    public int score(Project project, Employee employee) {

        Set<String> requiredTechs = project.getRequiredTechCodes(); // R

        if (requiredTechs == null || requiredTechs.isEmpty()) {
            return 0;
        }

        Set<String> experiencedTechs =
                techRequirementRepository.findExperiencedTechCodes(
                        employee.getUserId()
                ); // E

        long matchedCount =
                requiredTechs.stream()
                        .filter(experiencedTechs::contains)
                        .count();

        double ratio = (double) matchedCount / requiredTechs.size();

        return (int) Math.floor(ratio * MAX_SCORE);
    } */
}
