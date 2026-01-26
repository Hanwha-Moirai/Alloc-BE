package com.moirai.alloc.search.query.domain.policy;

import com.moirai.alloc.search.query.domain.condition.JobGradeRange;
import com.moirai.alloc.search.query.domain.condition.SeniorityRange;
import com.moirai.alloc.search.query.domain.vocabulary.JobGrade;
import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
import org.springframework.stereotype.Component;

@Component
public class SeniorityPolicy {

    public SeniorityRange inferFromJobGrade(JobGradeRange gradeRange) {

        if (gradeRange == null) {
            return null;
        }

        JobGrade minGrade = gradeRange.getMinGrade();
        JobGrade maxGrade = gradeRange.getMaxGrade();

        // 부장 이상만 포함 → 시니어로 간주
        if (minGrade.getLevel() >= JobGrade.DIRECTOR.getLevel()) {
            return new SeniorityRange(
                    SeniorityLevel.SENIOR,
                    SeniorityLevel.SENIOR
            );
        }

        // 주임 이하 → 주니어 ~ 미들로 간주
        if (maxGrade.getLevel() <= JobGrade.ASSOCIATE.getLevel()) {
            return new SeniorityRange(
                    SeniorityLevel.JUNIOR,
                    SeniorityLevel.MIDDLE
            );
        }

        // 애매한 경우 (과장~차장 등)
        // → 추론하지 않음 (명시적 조건 우선)
        return null;
    }
}
