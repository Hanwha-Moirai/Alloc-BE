package com.moirai.alloc.search.query.domain.intent;

import com.moirai.alloc.search.query.domain.condition.*;
import com.moirai.alloc.search.query.domain.vocabulary.ExperienceDomain;
import com.moirai.alloc.search.query.domain.vocabulary.JobRole;
import com.moirai.alloc.search.query.domain.vocabulary.WorkingType;
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
public class SearchIntent {
    // GPT가 해석한 의미 결과; 고정된 json 구조
    private String freeText;

    private Integer activeProjectCount;
    private ComparisonType comparisonType;

    // 기술 조건
    private List<SkillCondition> skillConditions; // 레벨
    private LogicalOperator skillLogicalOperator;

    //근무/조직
    private WorkingType workingType;

    //직급 범위
    private SeniorityRange seniorityRange;
    private JobGradeRange jobGradeRange;

    private JobRole jobRole;
    private String department;

    private Set<ExperienceDomain> experienceDomains;
    //응답 갯수 제한
    private Integer limit;
}