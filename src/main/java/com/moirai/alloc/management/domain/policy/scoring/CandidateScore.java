package com.moirai.alloc.management.domain.policy.scoring;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CandidateScore {
    private final Long userId;

    private final int skillScore;
    private final int experienceScore;
    private final int availabilityScore;
    private final int roleScore;

    private final int totalScore;
    @Builder
    private CandidateScore(Long userId,
                           int skillScore,
                           int experienceScore,
                           int availabilityScore,
                           int roleScore) {
        this.userId = userId;
        this.skillScore = skillScore;
        this.experienceScore = experienceScore;
        this.availabilityScore = availabilityScore;
        this.roleScore = roleScore;
        this.totalScore =
                skillScore + experienceScore + availabilityScore + roleScore;
    }
}
//도메인 내부 계산 결과 객체 (Value Object)
// 원점수 묶음(결과 묶음) VO