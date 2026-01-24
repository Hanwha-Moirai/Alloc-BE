package com.moirai.alloc.search.query.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillCondition {
    private String tech;
    private SkillLevel skillLevel;
    private ComparisonType comparisonType;
}
