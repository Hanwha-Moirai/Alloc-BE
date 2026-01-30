package com.moirai.alloc.search.query.infra.intent;

import com.moirai.alloc.search.query.domain.condition.*;
import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.domain.vocabulary.ExperienceDomain;
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

    private final ConversationContext context;
    private Set<JobRole> detectedJobRoles;

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

    private static final Map<String, ExperienceDomain> DOMAIN_RULES = Map.ofEntries(
            Map.entry("금융", ExperienceDomain.FINANCE),
            Map.entry("핀테크", ExperienceDomain.FINANCE),
            Map.entry("공공", ExperienceDomain.PUBLIC),
            Map.entry("커머스", ExperienceDomain.RETAIL),
            Map.entry("쇼핑", ExperienceDomain.RETAIL),
            Map.entry("물류", ExperienceDomain.LOGISTICS),
            Map.entry("제조", ExperienceDomain.MANUFACTURING)
    );

    public SearchIntent parse(String conversationId, String nl) {

        SearchIntent incoming = parseRule(nl);
        SearchIntent previous = context.getLastIntent(conversationId);

        SearchIntent merged = (previous == null)
                ? incoming
                : merge(previous, incoming);

        context.saveIntent(conversationId, merged);
        return merged;
    }

    // =========================
    // 1) rule 기반 파싱
    // =========================
    private SearchIntent parseRule(String nl) {

        SearchIntent.SearchIntentBuilder b = SearchIntent.builder();
        b.freeText(normalizeFreeText(nl));

        parseJobRole(nl, b);          // 직군
        parseSeniority(nl, b);        // 직위/시니어리티
        parseJobGrade(nl, b);         // 직급(인턴~임원)
        parseProjectCount(nl, b);     // 프로젝트 수
        parseSkills(nl, b);           // 기술
        parseExperienceDomain(nl, b); // 경험 도메인

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

    // ✅ 인턴~임원까지 전부 커버
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

    private void parseSkills(String nl, SearchIntent.SearchIntentBuilder b) {
        String lower = nl.toLowerCase();
        List<SkillCondition> skills = new ArrayList<>();

        for (var entry : SKILL_RULES.entrySet()) {
            String key = entry.getKey();
            if (nl.contains(key) || lower.contains(key)) {
                addSkill(skills, nl, entry.getValue());
            }
        }

        if (!skills.isEmpty()) {
            b.skillConditions(skills);
        }
    }

    private void parseExperienceDomain(String nl, SearchIntent.SearchIntentBuilder b) {
        for (var entry : DOMAIN_RULES.entrySet()) {
            if (nl.contains(entry.getKey())) {
                b.experienceDomain(entry.getValue());
                return;
            }
        }
    }

    // =========================
    // 2) intent 병합
    // =========================
    private SearchIntent merge(SearchIntent prev, SearchIntent cur) {

        return SearchIntent.builder()
                .freeText(mergeFreeText(prev.getFreeText(), cur.getFreeText()))

                .jobRole(cur.getJobRole() != null ? cur.getJobRole() : prev.getJobRole())

                .seniorityRange(cur.getSeniorityRange() != null ? cur.getSeniorityRange() : prev.getSeniorityRange())
                .jobGradeRange(cur.getJobGradeRange() != null ? cur.getJobGradeRange() : prev.getJobGradeRange())

                .activeProjectCount(cur.getActiveProjectCount() != null ? cur.getActiveProjectCount() : prev.getActiveProjectCount())
                .projectCountcomparisonType(cur.getProjectCountcomparisonType() != null ? cur.getProjectCountcomparisonType() : prev.getProjectCountcomparisonType())

                .skillConditions(mergeSkillConditions(prev.getSkillConditions(), cur.getSkillConditions()))

                .experienceDomain(cur.getExperienceDomain() != null ? cur.getExperienceDomain() : prev.getExperienceDomain())

                .department(cur.getDepartment() != null ? cur.getDepartment() : prev.getDepartment())
                .limit(cur.getLimit() != null ? cur.getLimit() : prev.getLimit())

                // 질문 카운트는 Orchestrator가 관리하므로 여기서는 prev 값 유지
                .questionCount(prev.getQuestionCount())
                .build();
    }

    // =========================
    // merge helpers
    // =========================
    private List<SkillCondition> mergeSkillConditions(List<SkillCondition> prev, List<SkillCondition> cur) {
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
        if (level == null) {
            // 레벨 없으면 기본 레벨을 두거나, EQUAL로 두는 방식 선택 가능
            // MVP에서는 LV1로 기본값 처리(혹은 null이면 추가 안 하는 방식도 가능)
            level = SkillLevel.LV1;
        }

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
        // 주의: "3" 같은 숫자는 프로젝트 개수랑 섞일 수 있음 → LV3, LV2, LV1 우선
        if (nl.contains("LV3")) return SkillLevel.LV3;
        if (nl.contains("LV2")) return SkillLevel.LV2;
        if (nl.contains("LV1")) return SkillLevel.LV1;
        return null;
    }
    private String normalizeFreeText(String nl) {
        return nl
                .replace("찾아줘", "")
                .replace("추천", "")
                .replace("사람", "")
                .trim();
    }

}
