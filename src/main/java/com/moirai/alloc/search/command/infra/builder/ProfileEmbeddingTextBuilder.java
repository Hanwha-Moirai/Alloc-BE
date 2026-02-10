package com.moirai.alloc.search.command.infra.builder;


import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;

import java.util.Map;

public class ProfileEmbeddingTextBuilder {

    private ProfileEmbeddingTextBuilder() {}

    public static String build(
            Employee employee,
            Map<String, SkillLevel> techSkills,
            String experienceDomainText
    ) {
        String techText =
                techSkills == null || techSkills.isEmpty()
                        ? ""
                        : String.join(", ", techSkills.keySet());

        return String.format(
                "%s. %s. 주요 기술 %s. 프로젝트 경험 %s.",
                employee.getJob().getJobName(),                 // 직군
                employee.getTitleStandard().getTitleName(),     // 직급
                techText,                                       // 기술들
                experienceDomainText                            // 프로젝트 제목
        );
    }
}