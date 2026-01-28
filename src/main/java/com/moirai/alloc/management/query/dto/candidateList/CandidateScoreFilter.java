package com.moirai.alloc.management.query.dto.candidateList;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CandidateScoreFilter {
    // 0 ~ 100 (or 그 이상 허용)
    private final int skill;
    private final int experience;
    private final int availability;
}
