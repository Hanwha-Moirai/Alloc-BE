package com.moirai.alloc.search.query.domain.intent;

import com.moirai.alloc.search.query.domain.condition.SkillCondition;
import com.moirai.alloc.search.query.domain.vocabulary.ExperienceDomain;
import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
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
                .skillLogicalOperator(
                        incoming.getSkillLogicalOperator() != null
                        ? incoming.getSkillLogicalOperator()
                                :previous.getSkillLogicalOperator()
                )
                // 근무 형태
                .workingType(
                        incoming.getWorkingType() != null
                                ? incoming.getWorkingType()
                                : previous.getWorkingType()
                )
                // 직급 범위 overwrite / relax
                .seniorityRange(
                        mergeRange(
                                previous.getSeniorityRange(),
                                incoming.getSeniorityRange(),
                                rawMessage
                        )
                )
                .jobGradeRange(
                        mergeRange(
                                previous.getJobGradeRange(),
                                incoming.getJobGradeRange(),
                                rawMessage
                        )
                )

                // 직무 / 부서 overwrite
                .jobRole(
                        incoming.getJobRole() != null
                                ? incoming.getJobRole()
                                : previous.getJobRole()
                )
                .department(
                        incoming.getDepartment() != null
                                ? incoming.getDepartment()
                                : previous.getDepartment()
                )

                // 경험 도메인 병합
                .experienceDomains(
                        mergeExperienceDomains(
                                previous.getExperienceDomains(),
                                incoming.getExperienceDomains()
                        )
                )

                // limit overwrite
                .limit(
                        incoming.getLimit() != null
                                ? incoming.getLimit()
                                : previous.getLimit()
                )
                .build();
    }
    private <T> T mergeRange(
            T previous,
            T incoming,
            String rawMessage
    ) {
        if (isRelaxRequest(rawMessage)) {
            return null; // 제한 해제
        }

        if (incoming != null) {
            return incoming; // overwrite
        }

        return previous; // 유지
    }
    private <T> Set<T> mergeSet(
            Set<T> previous,
            Set<T> incoming,
            String rawMessage
    ) {
        if (isRelaxRequest(rawMessage)) {
            return null; // 제한 해제
        }

        if (incoming != null && !incoming.isEmpty()) {
            return incoming; // overwrite
        }

        return previous;
    }

    private boolean isRelaxRequest(String message) {
        if (message == null) return false;

        return message.contains("포함")
                || message.contains("까지")
                || message.contains("다 보여")
                || message.contains("전체")
                || message.contains("상관없이");
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
    private Set<ExperienceDomain> mergeExperienceDomains(
            Set<ExperienceDomain> previous,
            Set<ExperienceDomain> incoming
    ) {
        if (incoming == null || incoming.isEmpty()) {
            return previous;
        }
        if (previous == null || previous.isEmpty()) {
            return incoming;
        }

        Set<ExperienceDomain> merged = new HashSet<>(previous);
        merged.addAll(incoming);
        return merged;
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
