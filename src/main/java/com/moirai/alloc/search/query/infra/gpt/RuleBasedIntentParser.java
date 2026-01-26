package com.moirai.alloc.search.query.infra.gpt;

import com.moirai.alloc.search.query.domain.condition.ComparisonType;
import com.moirai.alloc.search.query.domain.intent.SearchIntent;

import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuleBasedIntentParser {

    public SearchIntent parse(String nl) {
        String normalized = nl.toLowerCase();

        SearchIntent.SearchIntentBuilder builder = SearchIntent.builder();

        parseSeniority(normalized, builder);
        parseWorkingType(normalized, builder);
        parseJobRole(normalized, builder);
        parseTechAndSkill(normalized, builder);
        parseExperienceDomain(normalized, builder);
        parseProjectCount(normalized, builder);
        parseLimit(normalized, builder);

        // fallback
        builder.freeText(nl);

        return builder.build();
    }
    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
    private void parseSeniority(
            String nl,
            SearchIntent.SearchIntentBuilder builder
    ) {
        for (var entry : KeywordDictionary.SENIORITY_KEYWORDS.entrySet()) {
            if (containsAny(nl, entry.getValue())) {
                builder.seniorityLevel(entry.getKey());
                return; // 가장 먼저 매칭된 것 1개만
            }
        }
    }
    private void parseWorkingType(
            String nl,
            SearchIntent.SearchIntentBuilder builder
    ) {
        if (nl.contains("intern") || nl.contains("인턴")) {
            builder.workingType(WorkingType.INTERN);
        } else if (nl.contains("vendor") || nl.contains("외주")) {
            builder.workingType(WorkingType.VENDOR);
        } else if (nl.contains("계약직")) {
            builder.workingType(WorkingType.CONTRACT);
        }
    }
    private void parseJobRole(
            String nl,
            SearchIntent.SearchIntentBuilder builder
    ) {
        for (var entry : KeywordDictionary.JOB_ROLE_KEYWORDS.entrySet()) {
            if (containsAny(nl, entry.getValue())) {
                builder.jobTitle(entry.getKey().name());
                return;
            }
        }
    }
    private void parseTechAndSkill(
            String nl,
            SearchIntent.SearchIntentBuilder builder
    ) {
        List<String> techs = new ArrayList<>();

        for (var entry : KeywordDictionary.TECH_KEYWORDS.entrySet()) {
            if (containsAny(nl, entry.getValue())) {
                techs.add(entry.getKey().name());
            }
        }

        if (!techs.isEmpty()) {
            builder.techName(techs);
        }

        if (nl.contains("lv3") || nl.contains("레벨3") || nl.contains("고급")) {
            builder.skillLevel(SkillLevel.LV3);
        } else if (nl.contains("lv2") || nl.contains("레벨2") || nl.contains("중간")) {
            builder.skillLevel(SkillLevel.LV2);
        } else if (nl.contains("lv1") || nl.contains("레벨1") || nl.contains("초급")) {
            builder.skillLevel(SkillLevel.LV1);
        }
    }
    private void parseExperienceDomain(
            String nl,
            SearchIntent.SearchIntentBuilder builder
    ) {
        for (var entry : KeywordDictionary.EXPERIENCE_DOMAIN_KEYWORDS.entrySet()) {
            if (containsAny(nl, entry.getValue())) {
                builder.freeText(entry.getKey().name());
            }
        }
    }
    private static final Pattern PROJECT_COUNT_PATTERN =
            Pattern.compile("(\\d+)\\s*개\\s*(이상|이하|초과|미만|하는)");

    private void parseProjectCount(
            String nl,
            SearchIntent.SearchIntentBuilder builder
    ) {
        Matcher m = PROJECT_COUNT_PATTERN.matcher(nl);
        if (!m.find()) return;

        int count = Integer.parseInt(m.group(1));
        String op = m.group(2);

        builder.activeProjectCount(count);

        switch (op) {
            case "이상" -> builder.comparisonType(ComparisonType.GREATER_THAN_OR_EQUAL);
            case "이하" -> builder.comparisonType(ComparisonType.LESS_THAN_OR_EQUAL);
            case "초과" -> builder.comparisonType(ComparisonType.GREATER_THAN);
            case "미만" -> builder.comparisonType(ComparisonType.LESS_THAN);
            case "하는" -> builder.comparisonType(ComparisonType.EQUAL);
        }
    }
    private static final Pattern LIMIT_PATTERN =
            Pattern.compile("(\\d+)\\s*명");

    private void parseLimit(
            String nl,
            SearchIntent.SearchIntentBuilder builder
    ) {
        Matcher m = LIMIT_PATTERN.matcher(nl);
        if (m.find()) {
            builder.limit(Integer.parseInt(m.group(1)));
        }
    }

}
