package com.moirai.alloc.search.query.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@NoArgsConstructor
public class SearchCondition {
    // 이런 <직무,기술,숙련도 etc>찾아줘
    // 검색용 조건; 검색에 쓰기 좋은 형태로 한 번 더 정리(dto)
    // 사람 검색에 필요한 조건 스펙을 먼저 고정한다.
    // 검색 조건 전달(무엇을 찾을지..검색 기준 필터)
    // 직무, 기술, 기술 숙련도, 투입 중 프로젝트 수, 부서, 재직유형

    private String freeText; // 기본 검색 (항상 사용 가능)

    // 숫자, ENUM (거의 확실)
    private Integer activeProjectCount;
    private ComparisonType comparisonType; // 이게 뭔용도임?
    private WorkingType workingType;
    private SkillLevel skillLevel;

    // 정확하게 적은 경우에 채워지는 필드
    private String job;
    private List<String> techs;
    private String department;

    private Integer limit;

}
