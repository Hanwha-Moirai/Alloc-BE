package com.moirai.alloc.search.query.domain.condition;

import com.moirai.alloc.search.query.domain.vocabulary.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

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
    private LogicalOperator logicalOperator;

    // 숫자 비교 조건
    private Integer activeProjectCount;
    private ComparisonType comparisonType;

    // enum 기반 필터
    private WorkingType workingType;

    private SeniorityRange seniorityRange;
    private JobGradeRange jobGradeRange;

    // 정확한 매칭 필터
    private JobRole jobRole;
    private String department; //부서명

    private Set<ExperienceDomain> experienceDomains;
    private LogicalOperator experienceOperator;

    private ProjectType projectType;

    private Integer limit;

}
