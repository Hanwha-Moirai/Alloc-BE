package com.moirai.alloc.search.query.domain.intent;

import com.moirai.alloc.search.query.domain.condition.*;
import com.moirai.alloc.search.query.domain.vocabulary.ExperienceDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIntent {
    // GPT가 해석한 의미 결과; 고정된 json 구조
    private String freeText; // 검색 보조용

    private Integer activeProjectCount;
    private ComparisonType projectCountcomparisonType;

    // 기술 조건
    private List<SkillCondition> skillConditions; // 레벨

    //직급 범위
    private SeniorityRange seniorityRange;
    private JobGradeRange jobGradeRange;

    private String department;

    private ExperienceDomain experienceDomain; // 질문 축 판단용
    //응답 갯수 제한
    private Integer limit;
}
