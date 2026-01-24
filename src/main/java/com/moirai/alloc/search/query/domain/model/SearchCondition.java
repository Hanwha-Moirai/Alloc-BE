package com.moirai.alloc.search.query.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCondition {
    // 검색용 조건; 검색에 쓰기 좋은 형태로 정리(dto)
    // 사람 검색에 필요한 조건 스펙을 먼저 고정

    //자연어; fallback, multi_match 용도
    private String freeText; // 기본 검색 (항상 사용 가능)

    // 숫자 비교 조건
    private Integer activeProjectCount;
    private ComparisonType comparisonType;

    // enum 기반 필터
    private WorkingType workingType;
    private SeniorityLevel seniorityLevel; // 추상화된 직급 작성(시니어, 주니어 등)

    // 기술 조건; map 대응
    private String tech;
    private SkillLevel skillLevel;

    // 정확한 매칭 필터
    private String title; // 정확한 직급 작성(부장, 차장 등_
    private String department; //부서명

    private Integer limit;

}
