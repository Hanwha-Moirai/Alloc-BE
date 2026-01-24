package com.moirai.alloc.search.query.domain.condition;

import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillCondition {
    private String techName;
    private SkillLevel skillLevel;
    private ComparisonType comparisonType;
}
