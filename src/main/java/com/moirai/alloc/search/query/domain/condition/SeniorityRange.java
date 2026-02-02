package com.moirai.alloc.search.query.domain.condition;

import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeniorityRange {
    private SeniorityLevel minLevel;
    private SeniorityLevel maxLevel;

}
