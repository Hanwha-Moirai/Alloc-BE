package com.moirai.alloc.search.query.service.validation;

import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.domain.vocabulary.JobRole;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class SearchIntentValidator {

    public Optional<String> validate(SearchIntent intent) {

        // 1. 아무 의미도 없는 입력
        if (isEmptyIntent(intent)) {
            return Optional.of("검색할 수 있는 조건을 이해하지 못했습니다.");
        }

        // 2. 직급/시니어리티 모순
        if (isContradictorySeniority(intent)) {
            return Optional.of("서로 모순되는 직급/직위 조건이 포함되어 있습니다.");
        }

        // 3. 질문 다 했는데도 핵심 조건 없음
        if (isInsufficient(intent)) {
            return Optional.of("검색에 필요한 정보가 부족합니다.");
        }

        return Optional.empty(); // 정상
    }

    private boolean isEmptyIntent(SearchIntent intent) {
        return intent.getSeniorityRange() == null
                && intent.getJobGradeRange() == null
                && (intent.getSkillConditions() == null || intent.getSkillConditions().isEmpty())
                && intent.getExperienceDomain() == null
                && (intent.getFreeText() == null || intent.getFreeText().isBlank());
    }

    private boolean isContradictorySeniority(SearchIntent intent) {
        if (intent.getJobGradeRange() == null) return false;

        int min = intent.getJobGradeRange().getMinGrade().getLevel();
        int max = intent.getJobGradeRange().getMaxGrade().getLevel();

        // 예: INTERN(1) ~ DIRECTOR(7) 같이 말도 안 되는 경우
        return (max - min) >= 5;
    }

    private boolean isInsufficient(SearchIntent intent) {
        return (intent.getSkillConditions() == null || intent.getSkillConditions().isEmpty())
                && intent.getExperienceDomain() == null
                && intent.getSeniorityRange() == null
                && intent.getJobGradeRange() == null;
    }

}
