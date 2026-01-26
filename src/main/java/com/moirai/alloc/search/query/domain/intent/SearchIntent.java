package com.moirai.alloc.search.query.domain.intent;

import com.moirai.alloc.search.query.domain.condition.*;
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
    private String freeText;

    private Integer activeProjectCount;
    private ComparisonType comparisonType;

    // 기술 조건
    private List<SkillCondition> skillConditions; // 레벨

    //직급 범위
    private SeniorityRange seniorityRange;
    private JobGradeRange jobGradeRange;

    private String department;
    //응답 갯수 제한
    private Integer limit;
}