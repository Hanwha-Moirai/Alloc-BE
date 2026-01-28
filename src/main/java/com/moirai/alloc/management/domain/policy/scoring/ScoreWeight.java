package com.moirai.alloc.management.domain.policy.scoring;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScoreWeight {

    private final double skill;
    private final double experience;
    private final double availability;
}
// 프로젝트 타입별 해석 로직
