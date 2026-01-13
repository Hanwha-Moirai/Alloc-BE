package com.moirai.alloc.management.command.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScoredCandidateDTO {
    private Long userId;
    private int fitnessScore;
}
