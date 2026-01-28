package com.moirai.alloc.management.query.policy;

import com.moirai.alloc.management.domain.policy.scoring.ScoreWeight;
import com.moirai.alloc.management.query.dto.candidateList.CandidateScoreFilter;
import org.springframework.stereotype.Component;

@Component
public class ScoreWeightAdjuster {

    public ScoreWeight adjust(
            ScoreWeight base,
            CandidateScoreFilter filter
    ) {
        // filter가 null이면 기본 가중치 그대로 사용
        if (filter == null) {
            return base;
        }

        return new ScoreWeight(
                base.getSkill() * (filter.getSkill() / 100.0),
                base.getExperience() * (filter.getExperience() / 100.0),
                base.getAvailability() * (filter.getAvailability() / 100.0)
        );
    }
}

