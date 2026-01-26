package com.moirai.alloc.search.query.domain.intent;

import com.moirai.alloc.search.query.domain.condition.SkillCondition;
import com.moirai.alloc.search.query.domain.vocabulary.ExperienceDomain;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SearchIntentMerger {
    public SearchIntent merge(
            SearchIntent previous,
            SearchIntent incoming,
            String rawMessage
    ) {
        if(previous == null){
            return incoming;
        }
        return SearchIntent.builder()
                //자유텍스트는 누적
                .freeText(mergeFreeText(previous.getFreeText(), incoming.getFreeText()))
                // 숫자 조건은 overwrite (현재 프로젝트하는 갯수)
                .activeProjectCount(
                        incoming.getActiveProjectCount() !=null
                        ?incoming.getActiveProjectCount()
                                : previous.getActiveProjectCount()
                )
                .comparisonType(
                        incoming.getComparisonType() != null
                        ? incoming.getComparisonType()
                                :previous.getComparisonType()
                )
                //기술 조건은 병합
                .skillConditions(
                        mergeSkillConditions(
                                previous.getSkillConditions(),
                                incoming.getSkillConditions()
                        )
                )
                // 직급 범위 overwrite / relax
                .seniorityRange(
                        mergeRange(
                                previous.getSeniorityRange(),
                                incoming.getSeniorityRange()
                        )
                )
                .jobGradeRange(
                        mergeRange(
                                previous.getJobGradeRange(),
                                incoming.getJobGradeRange()
                        )
                )
                .department(
                        incoming.getDepartment() != null
                                ? incoming.getDepartment()
                                : previous.getDepartment()
                )
                // limit overwrite
                .limit(
                        incoming.getLimit() != null
                                ? incoming.getLimit()
                                : previous.getLimit()
                )
                .build();
    }
    private <T> T mergeRange(T previous, T incoming) {
        return incoming != null ? incoming : previous;
    }

    /* ===============================
       SkillCondition 병합
       =============================== */

    private List<SkillCondition> mergeSkillConditions(
            List<SkillCondition> previous,
            List<SkillCondition> incoming
    ) {
        if (incoming == null || incoming.isEmpty()) {
            return previous;
        }
        if (previous == null || previous.isEmpty()) {
            return incoming;
        }

        Map<String, SkillCondition> merged = new LinkedHashMap<>();

        // 기존 조건
        for (SkillCondition sc : previous) {
            merged.put(sc.getTechName(), sc);
        }

        // 새 조건 → 같은 tech면 overwrite (정밀화 / 제외)
        for (SkillCondition sc : incoming) {
            merged.put(sc.getTechName(), sc);
        }

        return new ArrayList<>(merged.values());
    }
    private String mergeFreeText(String prev, String incoming) {
        if (incoming == null || incoming.isBlank()) {
            return prev;
        }
        if (prev == null || prev.isBlank()) {
            return incoming;
        }
        return prev + " " + incoming;
    }
}
