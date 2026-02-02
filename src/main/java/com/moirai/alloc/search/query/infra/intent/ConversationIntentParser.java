package com.moirai.alloc.search.query.infra.intent;

import com.moirai.alloc.search.query.domain.condition.*;
import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.domain.vocabulary.JobGrade;
import com.moirai.alloc.search.query.domain.vocabulary.JobRole;
import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ConversationIntentParser {

    // -------------------------
    // 룰 테이블(딕셔너리 파일 분리 X, 파서 내부 상수로 유지)
    // -------------------------
    private static final Map<String, String> SKILL_RULES = Map.ofEntries(
            Map.entry("자바", "JAVA"),
            Map.entry("java", "JAVA"),

            Map.entry("스프링", "SPRING"),
            Map.entry("spring", "SPRING"),

            Map.entry("파이썬", "PYTHON"),
            Map.entry("python", "PYTHON"),

            Map.entry("쿠버네티스", "KUBERNETES"),
            Map.entry("k8s", "KUBERNETES"),

            Map.entry("도커", "DOCKER"),
            Map.entry("docker", "DOCKER"),

            Map.entry("레디스", "REDIS"),
            Map.entry("redis", "REDIS"),

            Map.entry("카프카", "KAFKA"),
            Map.entry("kafka", "KAFKA"),

            Map.entry("mysql", "MYSQL"),
            Map.entry("mariadb", "MARIADB"),
            Map.entry("postgres", "POSTGRESQL"),

            Map.entry("오픈서치", "OPENSEARCH"),
            Map.entry("엘라스틱", "ELASTICSEARCH")
    );

    private static final Map<String, JobRole> JOB_ROLE_RULES = Map.ofEntries(
            Map.entry("백엔드", JobRole.BACKEND),
            Map.entry("백엔드개발자", JobRole.BACKEND),
            Map.entry("서버", JobRole.BACKEND),

            Map.entry("인프라", JobRole.INFRA),
            Map.entry("인프라엔지니어", JobRole.INFRA),
            Map.entry("devops", JobRole.INFRA),
            Map.entry("데브옵스", JobRole.INFRA),

            Map.entry("프론트", JobRole.FRONTEND),
            Map.entry("프론트엔드", JobRole.FRONTEND),

            Map.entry("데이터", JobRole.DATA),
            Map.entry("데이터엔지니어", JobRole.DATA),

            Map.entry("머신러닝", JobRole.ML),
            Map.entry("ml", JobRole.ML),
            Map.entry("ai", JobRole.ML),

            Map.entry("모바일", JobRole.MOBILE),
            Map.entry("안드로이드", JobRole.MOBILE),
            Map.entry("ios", JobRole.MOBILE)
    );

    public SearchIntent parse(String conversationId, String nl) {

        return parseRule(nl);
    }

    // =========================
    // 1) rule 기반 파싱
    // =========================
    private SearchIntent parseRule(String nl) {
        SearchIntent.SearchIntentBuilder b = SearchIntent.builder();
        b.freeText(normalizeFreeText(nl));

        parseJobGrade(nl, b);
        parseJobRole(nl, b);          // 직군
        parseSkills(nl, b);           // 기술
        parseSeniority(nl, b);        // 직위/시니어리티
        parseProjectCount(nl, b);     // 프로젝트 수
        return b.build();
    }

    private void parseJobRole(String nl, SearchIntent.SearchIntentBuilder b) {
        String lower = nl.toLowerCase();
        for (var e : JOB_ROLE_RULES.entrySet()) {
            if (nl.contains(e.getKey()) || lower.contains(e.getKey())) {
                b.jobRole(e.getValue());
                return;
            }
        }
    }

    private void parseSeniority(String nl, SearchIntent.SearchIntentBuilder b) {
        if (nl.contains("시니어")) {
            b.seniorityRange(new SeniorityRange(SeniorityLevel.SENIOR, SeniorityLevel.SENIOR));
            return;
        }
        if (nl.contains("미들") || nl.contains("중급")) {
            b.seniorityRange(new SeniorityRange(SeniorityLevel.MIDDLE, SeniorityLevel.MIDDLE));
            return;
        }
        if (nl.contains("주니어")) {
            b.seniorityRange(new SeniorityRange(SeniorityLevel.JUNIOR, SeniorityLevel.JUNIOR));
        }
    }

    // 인턴~임원까지 전부 커버
    private void parseJobGrade(String nl, SearchIntent.SearchIntentBuilder b) {
        if (nl.contains("인턴")) {
            b.jobGradeRange(new JobGradeRange(JobGrade.INTERN, JobGrade.INTERN));
            return;
        }
        if (nl.contains("사원")) {
            b.jobGradeRange(new JobGradeRange(JobGrade.STAFF, JobGrade.STAFF));
            return;
        }
        if (nl.contains("주임")) {
            b.jobGradeRange(new JobGradeRange(JobGrade.ASSOCIATE, JobGrade.ASSOCIATE));
            return;
        }
        if (nl.contains("대리")) {
            b.jobGradeRange(new JobGradeRange(JobGrade.SENIOR_ASSOCIATE, JobGrade.SENIOR_ASSOCIATE));
            return;
        }
        if (nl.contains("과장") || nl.contains("PM")) {
            b.jobGradeRange(new JobGradeRange(JobGrade.PROJECT_MANAGER, JobGrade.PROJECT_MANAGER));
            return;
        }
        if (nl.contains("차장")) {
            b.jobGradeRange(new JobGradeRange(JobGrade.SENIOR_MANAGER, JobGrade.SENIOR_MANAGER));
            return;
        }
        if (nl.contains("부장")) {
            b.jobGradeRange(new JobGradeRange(JobGrade.DIRECTOR, JobGrade.DIRECTOR));
            return;
        }
        if (nl.contains("임원")) {
            b.jobGradeRange(new JobGradeRange(JobGrade.EXECUTIVE, JobGrade.EXECUTIVE));
        }
    }

    private void parseProjectCount(String nl, SearchIntent.SearchIntentBuilder b) {
        if (!nl.contains("프로젝트")) return;

        Integer count = extractNumber(nl);
        if (count == null) return;

        if (nl.contains("이하")) {
            b.activeProjectCount(count);
            b.projectCountcomparisonType(ComparisonType.LESS_THAN_OR_EQUAL);
            return;
        }

        if (nl.contains("이상")) {
            b.activeProjectCount(count);
            b.projectCountcomparisonType(ComparisonType.GREATER_THAN_OR_EQUAL);
            return;
        }

        // 기본값: 정확히 N개
        b.activeProjectCount(count);
        b.projectCountcomparisonType(ComparisonType.EQUAL);
    }

    private void parseSkills(String nl, SearchIntent.SearchIntentBuilder b) {
        String lower = nl.toLowerCase();
        List<SkillCondition> skills = new ArrayList<>();
        SkillLevel level = extractSkillLevel(nl);
        ComparisonType comparison = extractComparisonType(nl);
        for (var entry : SKILL_RULES.entrySet()) {
            if (nl.contains(entry.getKey()) || lower.contains(entry.getKey())) {
                skills.add(new SkillCondition(
                        entry.getValue(),
                        level != null ? level : SkillLevel.LV1,
                        comparison
                ));
            }
        }

        if (!skills.isEmpty()) {
            b.skillConditions(skills);
        }
    }

    private Integer extractNumber(String nl) {
        Matcher m = Pattern.compile("(\\d+)").matcher(nl);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private SkillLevel extractSkillLevel(String nl) {
        // 주의: "3" 같은 숫자는 프로젝트 개수랑 섞일 수 있음 → LV3, LV2, LV1 우선
        if (nl.contains("LV3")) return SkillLevel.LV3;
        if (nl.contains("LV2")) return SkillLevel.LV2;
        if (nl.contains("LV1")) return SkillLevel.LV1;
        return null;
    }
    private ComparisonType extractComparisonType(String nl) {
        if (nl.contains("이상")) return ComparisonType.GREATER_THAN_OR_EQUAL;
        if (nl.contains("이하")) return ComparisonType.LESS_THAN_OR_EQUAL;
        return ComparisonType.GREATER_THAN_OR_EQUAL; // 기본값
    }

    private String normalizeFreeText(String nl) {
        return nl
                .replace("찾아줘", "")
                .replace("추천", "")
                .replace("사람", "")
                .trim();
    }

}
