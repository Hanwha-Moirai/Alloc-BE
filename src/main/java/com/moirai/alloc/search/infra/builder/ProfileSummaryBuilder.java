package com.moirai.alloc.search.infra.builder;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.search.query.domain.model.SkillLevel;

import java.util.Map;
import java.util.stream.Collectors;

public class ProfileSummaryBuilder {
    // 검색에서 여러 필드를 매번 조합하면 효율 떨어짐으로
    // 이름 + 직급 + 기술 + 경험을 하나의 summary문자열로 미리 조합
    // 성능 최적화 + 검색 품질 향상
    private ProfileSummaryBuilder() {}

    public static String build(
            Employee employee,
            Map<String, SkillLevel> techSkills,
            String experienceDomainText
    ) {
        String techSummary = String.join(", ", techSkills.keySet());

        return String.format(
                "%s %s, 기술: %s, 경험: %s %s",
                employee.getTitleStandard().getTitleName(),
                employee.getUser().getUserName(),
                techSummary,
                experienceDomainText
        );
    }
}