package com.moirai.alloc.search.query.infra.gpt;

import com.moirai.alloc.search.query.domain.condition.ComparisonType;
import com.moirai.alloc.search.query.domain.condition.JobGradeRange;
import com.moirai.alloc.search.query.domain.condition.SeniorityRange;
import com.moirai.alloc.search.query.domain.condition.SkillCondition;
import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.domain.vocabulary.JobGrade;
import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuleBasedIntentParser implements SearchIntentParser {
    // 초안 제시
    @Override
    public SearchIntent parse(String nl) {

        SearchIntent.SearchIntentBuilder b = SearchIntent.builder();
        b.freeText(nl); // 항상 누적

        // =========================
        // 프로젝트 개수
        // =========================
        if (nl.contains("이하")) {
            Integer count = extractNumber(nl);
            if (count != null) {
                b.activeProjectCount(count);
                b.comparisonType(ComparisonType.LESS_THAN_OR_EQUAL);
            }
        }

        if (nl.contains("이상")) {
            Integer count = extractNumber(nl);
            if (count != null) {
                b.activeProjectCount(count);
                b.comparisonType(ComparisonType.GREATER_THAN_OR_EQUAL);
            }
        }

        // =========================
        // 시니어 / 주니어
        // =========================
        // 시니어
        if (nl.contains("시니어")) {
            b.seniorityRange(
                    new SeniorityRange(SeniorityLevel.SENIOR, SeniorityLevel.SENIOR)
            );
        }

// 미들
        if (nl.contains("미들") || nl.contains("중급")) {
            b.seniorityRange(
                    new SeniorityRange(SeniorityLevel.MIDDLE, SeniorityLevel.MIDDLE)
            );
        }

// 주니어
        if (nl.contains("주니어")) {
            b.seniorityRange(
                    new SeniorityRange(SeniorityLevel.JUNIOR, SeniorityLevel.JUNIOR)
            );
        }

        // =========================
        // 직급 (인턴 ~ 주임 등)
        // =========================
        if (nl.contains("인턴") || nl.contains("주임")) {
            b.jobGradeRange(
                    new JobGradeRange(JobGrade.INTERN, JobGrade.ASSOCIATE)
            );
        }

        if (nl.contains("대리") || nl.contains("과장")) {
            b.jobGradeRange(
                    new JobGradeRange(JobGrade.ASSOCIATE, JobGrade.PROJECT_MANAGER)
            );
        }

        if (nl.contains("차장") || nl.contains("부장")) {
            b.jobGradeRange(
                    new JobGradeRange(JobGrade.SENIOR_MANAGER, JobGrade.EXECUTIVE)
            );
        }

        // =========================
        // 기술 조건
        // =========================
        List<SkillCondition> skills = new ArrayList<>();

        if (nl.contains("자바")) {
            addSkill(skills, nl, "JAVA");
        }
        if (nl.contains("파이썬")) {
            addSkill(skills, nl, "PYTHON");
        }
        if(nl.contains("쿠버네티스")) {
            addSkill(skills, nl, "KUBERNETES");
        }

        if (!skills.isEmpty()) {
            b.skillConditions(skills);
        }

        return b.build();
    }

    // =========================
    // helpers
    // =========================

    private void addSkill(List<SkillCondition> list, String nl, String tech) {

        if (nl.contains("이상")) {
            list.add(new SkillCondition(
                    tech,
                    extractSkillLevel(nl),
                    ComparisonType.GREATER_THAN_OR_EQUAL
            ));
            return;
        }

        if (nl.contains("이하")) {
            list.add(new SkillCondition(
                    tech,
                    extractSkillLevel(nl),
                    ComparisonType.LESS_THAN_OR_EQUAL
            ));
            return;
        }

        // 기본: 정확 레벨
        SkillLevel level = extractSkillLevel(nl);
        if (level != null) {
            list.add(new SkillCondition(
                    tech,
                    level,
                    ComparisonType.EQUAL
            ));
        }
    }

    private Integer extractNumber(String nl) {
        Matcher m = Pattern.compile("(\\d+)").matcher(nl);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private SkillLevel extractSkillLevel(String nl) {
        if (nl.contains("LV3") || nl.contains("3")) return SkillLevel.LV3;
        if (nl.contains("LV2") || nl.contains("2")) return SkillLevel.LV2;
        if (nl.contains("LV1") || nl.contains("1")) return SkillLevel.LV1;
        return null;
    }
}
