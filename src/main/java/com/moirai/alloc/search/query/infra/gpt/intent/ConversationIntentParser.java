package com.moirai.alloc.search.query.infra.gpt.intent;

import com.moirai.alloc.search.query.domain.condition.ComparisonType;
import com.moirai.alloc.search.query.domain.condition.JobGradeRange;
import com.moirai.alloc.search.query.domain.condition.SeniorityRange;
import com.moirai.alloc.search.query.domain.condition.SkillCondition;
import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.domain.vocabulary.ExperienceDomain;
import com.moirai.alloc.search.query.domain.vocabulary.JobGrade;
import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
@Component
@RequiredArgsConstructor
public class ConversationIntentParser {

    private final ConversationContext context;

    public SearchIntent parse(String conversationId, String nl) {

        SearchIntent incoming = parseRule(nl);
        SearchIntent previous = context.getLastIntent(conversationId);

        SearchIntent merged =
                previous == null ? incoming : merge(previous, incoming);

        context.saveIntent(conversationId, merged);
        return merged;
    }


    // =========================
    // 1. rule 기반 파싱
    // =========================
    private SearchIntent parseRule(String nl) {

        SearchIntent.SearchIntentBuilder b = SearchIntent.builder();
        b.freeText(nl);

        parseProjectCount(nl, b);
        parseSeniority(nl, b);
        parseJobGrade(nl, b);
        parseSkills(nl, b);
        parseExperienceDomain(nl, b);

        return b.build();
    }

    // -------------------------
    // parsing helpers
    // -------------------------

    private void parseProjectCount(String nl, SearchIntent.SearchIntentBuilder b) {
        Integer count = extractNumber(nl);
        if (count == null) return;

        if (nl.contains("이하")) {
            b.activeProjectCount(count);
            b.projectCountcomparisonType(ComparisonType.LESS_THAN_OR_EQUAL);
        }

        if (nl.contains("이상")) {
            b.activeProjectCount(count);
            b.projectCountcomparisonType(ComparisonType.GREATER_THAN_OR_EQUAL);
        }
    }

    private void parseSeniority(String nl, SearchIntent.SearchIntentBuilder b) {
        if (nl.contains("시니어")) {
            b.seniorityRange(
                    new SeniorityRange(SeniorityLevel.SENIOR, SeniorityLevel.SENIOR)
            );
            return;
        }

        if (nl.contains("미들") || nl.contains("중급")) {
            b.seniorityRange(
                    new SeniorityRange(SeniorityLevel.MIDDLE, SeniorityLevel.MIDDLE)
            );
            return;
        }

        if (nl.contains("주니어")) {
            b.seniorityRange(
                    new SeniorityRange(SeniorityLevel.JUNIOR, SeniorityLevel.JUNIOR)
            );
        }
    }

    private void parseJobGrade(String nl, SearchIntent.SearchIntentBuilder b) {
        if (nl.contains("인턴") || nl.contains("주임")) {
            b.jobGradeRange(
                    new JobGradeRange(JobGrade.INTERN, JobGrade.ASSOCIATE)
            );
        }
    }

    private void parseSkills(String nl, SearchIntent.SearchIntentBuilder b) {
        List<SkillCondition> skills = new ArrayList<>();

        if (nl.contains("자바")) addSkill(skills, nl, "JAVA");
        if (nl.contains("파이썬")) addSkill(skills, nl, "PYTHON");

        if (!skills.isEmpty()) {
            b.skillConditions(skills);
        }
    }

    private void parseExperienceDomain(String nl, SearchIntent.SearchIntentBuilder b) {
        if (nl.contains("금융")) {
            b.experienceDomain(ExperienceDomain.FINANCE);
        }
    }

    // =========================
    // 2. intent 병합
    // =========================
    private SearchIntent merge(SearchIntent prev, SearchIntent cur) {

        return SearchIntent.builder()
                .freeText(mergeFreeText(prev.getFreeText(), cur.getFreeText()))
                .activeProjectCount(
                        cur.getActiveProjectCount() != null
                                ? cur.getActiveProjectCount()
                                : prev.getActiveProjectCount()
                )
                .projectCountcomparisonType(
                        cur.getProjectCountcomparisonType() != null
                                ? cur.getProjectCountcomparisonType()
                                : prev.getProjectCountcomparisonType()
                )
                .seniorityRange(
                        cur.getSeniorityRange() != null
                                ? cur.getSeniorityRange()
                                : prev.getSeniorityRange()
                )
                .jobGradeRange(
                        cur.getJobGradeRange() != null
                                ? cur.getJobGradeRange()
                                : prev.getJobGradeRange()
                )
                .experienceDomain(
                        cur.getExperienceDomain() != null
                                ? cur.getExperienceDomain()
                                : prev.getExperienceDomain()
                )
                .skillConditions(
                        mergeSkillConditions(
                                prev.getSkillConditions(),
                                cur.getSkillConditions()
                        )
                )
                .limit(
                        cur.getLimit() != null
                                ? cur.getLimit()
                                : prev.getLimit()
                )
                .build();
    }

    // =========================
    // merge helpers
    // =========================
    private List<SkillCondition> mergeSkillConditions(
            List<SkillCondition> prev,
            List<SkillCondition> cur
    ) {
        if (cur == null || cur.isEmpty()) return prev;
        if (prev == null || prev.isEmpty()) return cur;

        Map<String, SkillCondition> map = new LinkedHashMap<>();
        prev.forEach(sc -> map.put(sc.getTechName(), sc));
        cur.forEach(sc -> map.put(sc.getTechName(), sc));

        return new ArrayList<>(map.values());
    }

    private String mergeFreeText(String a, String b) {
        if (a == null || a.isBlank()) return b;
        if (b == null || b.isBlank()) return a;
        return a + " " + b;
    }

    // =========================
    // low-level extractors
    // =========================
    private void addSkill(List<SkillCondition> list, String nl, String tech) {
        SkillLevel level = extractSkillLevel(nl);
        if (level == null) return;

        ComparisonType type =
                nl.contains("이상") ? ComparisonType.GREATER_THAN_OR_EQUAL :
                        nl.contains("이하") ? ComparisonType.LESS_THAN_OR_EQUAL :
                                ComparisonType.EQUAL;

        list.add(new SkillCondition(tech, level, type));
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
