package com.moirai.alloc.search.query.domain.intent;

import com.moirai.alloc.search.query.domain.condition.*;
import com.moirai.alloc.search.query.domain.vocabulary.ExperienceDomain;
import com.moirai.alloc.search.query.domain.vocabulary.JobRole;
import lombok.*;

import java.util.List;
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIntent {
    // GPT가 해석한 의미 결과; 고정된 json 구조
    private String freeText; // 검색 보조용
    private float[] queryEmbedding; //유사도 검색용

    private Integer activeProjectCount;
    private ComparisonType projectCountcomparisonType;

    // 기술 조건
    private List<SkillCondition> skillConditions; // 레벨

    //직급 범위
    private SeniorityRange seniorityRange;
    private JobGradeRange jobGradeRange;

    private String department;
    private JobRole jobRole;

    private ExperienceDomain experienceDomain; // 질문 축 판단용
    //응답 갯수 제한
    private Integer limit;
    // 질문 횟수 제한
    private int questionCount;
}