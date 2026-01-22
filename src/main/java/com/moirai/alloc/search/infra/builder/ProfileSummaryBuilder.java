package com.moirai.alloc.search.infra.builder;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.search.query.domain.model.SkillLevel;

import java.util.Map;
import java.util.stream.Collectors;

public class ProfileSummaryBuilder {

    private ProfileSummaryBuilder() {}

    public static String build(
            Employee employee,
            Map<String, SkillLevel> techSkills,
            String experience
    ) {
        String techSummary = techSkills.keySet().stream()
                .collect(Collectors.joining(", "));

        return String.format(
                "%s %s, 기술: %s, %s",
                employee.getTitleStandard().getTitleName(),
                employee.getUser().getUserName(),
                techSummary,
                experience
        );
    }
}