package com.moirai.alloc.management.command.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScoredCandidateDTO {
    private Long userId;
    private int fitnessScore;
}
// 미선발, 점수만 가진 후보