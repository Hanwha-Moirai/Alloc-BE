package com.moirai.alloc.search.command.infra.builder;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;

import java.util.Map;
/**
 * 검색 성능 및 품질을 위한 summary 문자열 생성기.
 *
 * - OpenSearch multi_match 쿼리에서 fallback 필드로 사용
 * - 이름, 직급, 주요 기술을 하나의 문장으로 미리 조합
 * - 검색 시 동적 조합 비용을 줄이고 score 계산 안정화
 *
 * 현재는 단순한 형태지만,
 * 향후 프로젝트 경험, 역할 등을 확장 가능
 */

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
