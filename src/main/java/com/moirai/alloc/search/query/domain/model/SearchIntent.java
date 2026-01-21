package com.moirai.alloc.search.query.domain.model;

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
    // GPT가 해석한 의미; 고정된 json 구조
    private String freeText;

    private Integer activeProjectCount;
    private ComparisonType comparisonType;

    private SkillLevel skillLevel;
    private WorkingType workingType;

    private String job;
    private List<String> techs;
    private String department;
}