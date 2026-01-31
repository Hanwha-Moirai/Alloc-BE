package com.moirai.alloc.search.command.infra.builder;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;

import java.util.Map;

public class ProfileSummaryBuilder {
    // 사람을 한 문장으로 설명, freetext fallback 보조
    // 검색에서 여러 필드를 매번 조합하면 효율 떨어짐으로
    // 이름 + 직급 + 기술 + 경험을 하나의 summary문자열로 미리 조합
    // 성능 최적화 + 검색 품질 향상
    private ProfileSummaryBuilder() {}

    public static String build(
            Employee employee,
            Map<String, SkillLevel> techSkills
    ) {
        String techSummary =
                techSkills == null || techSkills.isEmpty()
                        ? ""
                        : String.join(", ", techSkills.keySet());

        return String.format(
                "%s %s %s",
                employee.getTitleStandard().getTitleName(),
                employee.getUser().getUserName(),
                techSummary
        );
    }
}
