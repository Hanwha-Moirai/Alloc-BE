package com.moirai.alloc.management.domain.policy.calculator.experience.fit;

import org.springframework.stereotype.Component;

@Component
public class TechExperiencePolicy {
/*
    private static final int MAX_SCORE = 10;

    private final ProjectTechRequirementRepository techRequirementRepository;


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
