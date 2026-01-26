package com.moirai.alloc.search.query.domain.condition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCondition {
    // 검색용 조건; 검색에 쓰기 좋은 형태로 정리(dto)
    // 사람 검색에 필요한 조건 스펙을 먼저 고정

    //자연어; fallback, multi_match 용도
    private String freeText; // 기본 검색 (항상 사용 가능)

    //기술 조건 (단일/ 복합 통일)
    private List<SkillCondition> skillConditions;

    // 숫자 비교 조건
    private Integer activeProjectCount;
    private ComparisonType projectCountComparisonType;
    private SeniorityRange seniorityRange;
    private JobGradeRange jobGradeRange;
    private String department;
    private Integer limit;

    public static SearchCondition of(
            String freeText,
            List<SkillCondition> skillConditions,
            Integer activeProjectCount,
            ComparisonType comparisonType,
            SeniorityRange seniorityRange,
            JobGradeRange jobGradeRange,
            String department,
            Integer limit
    ) {
        return SearchCondition.builder()
                .freeText(freeText)
                .skillConditions(skillConditions)
                .activeProjectCount(activeProjectCount)
                .projectCountComparisonType(
                        comparisonType != null
                                ? comparisonType
                                : ComparisonType.LESS_THAN_OR_EQUAL
                )
                .seniorityRange(seniorityRange)
                .jobGradeRange(jobGradeRange)
                .department(department)
                .limit(limit != null ? limit : 20)
                .build();
    }


}

