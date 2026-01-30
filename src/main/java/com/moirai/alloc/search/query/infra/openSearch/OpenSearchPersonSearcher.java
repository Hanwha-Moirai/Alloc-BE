package com.moirai.alloc.search.query.infra.openSearch;

import com.moirai.alloc.search.query.domain.condition.*;
import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.domain.vocabulary.JobRole;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import lombok.RequiredArgsConstructor;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenSearchPersonSearcher {
//    SearchCondition을 받아
//    검색 의도를 쿼리 형태로 ‘번역’하는 컴포넌트
    private final RestHighLevelClient client;


    public List<PersonDocument> search(SearchIntent intent) {
        // 1. SearchCondition 확인
        // 2. 조건별로 쿼리 조립
        // 3. OpenSearch 호출 (아직 구현 X)
        // 4. PersonDocument 리스트 반환
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        applyFreeText(intent, boolQuery);
        //applyJobRole(intent, boolQuery);
        applyExperienceDomain(intent, boolQuery);

        applyProjectCount(intent, boolQuery);
        applySeniorityRange(intent, boolQuery);
        applyJobGradeRange(intent, boolQuery);
        applySkillConditions(intent, boolQuery);

        // search request 생성
        SearchSourceBuilder source = new SearchSourceBuilder()
                .query(boolQuery)
                .size(resolveLimit(intent));

        // open search 호출
        return executeSearch(source);
    }
    private static final Map<String, String> TECH_KEY_TO_INDEX_KEY = Map.ofEntries(
            Map.entry("JAVA", "Java"),
            Map.entry("SPRING", "Spring Boot"),   // 너 인덱스는 Spring Boot로 들어가 있음
            Map.entry("PYTHON", "Python"),
            Map.entry("DOCKER", "Docker"),
            Map.entry("KUBERNETES", "Kubernetes"),
            Map.entry("REDIS", "Redis"),
            Map.entry("KAFKA", "Kafka"),
            Map.entry("MYSQL", "MySQL"),
            Map.entry("MARIADB", "MariaDB"),
            Map.entry("POSTGRESQL", "PostgreSQL"),
            Map.entry("OPENSEARCH", "OpenSearch"),
            Map.entry("ELASTICSEARCH", "Elasticsearch"),
            Map.entry("JPA", "JPA"),
            Map.entry("MYBATIS", "MyBatis"),
            Map.entry("NODEJS", "Node.js"),
            Map.entry("GO", "Go"),
            Map.entry("MONGODB", "MongoDB"),
            Map.entry("REACT", "React"),
            Map.entry("VUE", "Vue.js"),
            Map.entry("TYPESCRIPT", "TypeScript"),
            Map.entry("NEXTJS", "Next.js"),
            Map.entry("HTMLCSS", "HTML/CSS"),
            Map.entry("AWS", "AWS"),
            Map.entry("JENKINS", "Jenkins")
    );

    private List<String> resolveJobRoleKeywords(JobRole role) {
        return switch (role) {
            case BACKEND -> List.of("백엔드", "서버", "backend");
            case INFRA -> List.of("인프라", "devops", "infra");
            case FRONTEND -> List.of("프론트", "frontend");
            case DATA -> List.of("데이터", "data");
            case ML -> List.of("머신러닝", "ml", "ai");
            case MOBILE -> List.of("모바일", "android", "ios");
            default -> List.of();
        };
    }
//    private void applyJobRole(SearchIntent intent, BoolQueryBuilder bool) {
//        if (intent.getJobRole() == null) return;
//
//        BoolQueryBuilder roleQuery = QueryBuilders.boolQuery();
//
//        for (String keyword : resolveJobRoleKeywords(intent.getJobRole())) {
//            roleQuery.should(
//                    QueryBuilders.matchQuery("jobTitle", keyword).boost(3.0f)
//            );
//            roleQuery.should(
//                    QueryBuilders.matchQuery("profileSummary", keyword).boost(2.0f)
//            );
//        }
//
//        // 기술도 role 보강 점수로 추가
//        if (intent.getSkillConditions() != null) {
//            intent.getSkillConditions().forEach(sc ->
//                    roleQuery.should(
//                            QueryBuilders.matchQuery(
//                                    "profileSummary",
//                                    sc.getTechName()
//                            ).boost(2.5f)
//                    )
//            );
//        }
//
//        bool.should(roleQuery);
//    }

    private void applyExperienceDomain(SearchIntent intent, BoolQueryBuilder bool) {
        if (intent.getExperienceDomain() == null) return;

        String keyword = intent.getExperienceDomain().name().toLowerCase();

        bool.must(
                QueryBuilders.matchQuery(
                        "experienceDomainText",
                        keyword
                ).boost(2.5f)
        );
    }


    private void applyFreeText(SearchIntent intent, BoolQueryBuilder bool) {
        // 자유 자연어 검색, 동의어 없어도 동작
        if(intent.getFreeText() == null || intent.getFreeText().isBlank()){
            return; //해당되지 않으면 넘어가기
        }
        bool.should(
                QueryBuilders.multiMatchQuery(intent.getFreeText())
                        .field("experienceDomainText", 5.0f)
                        .field("profileSummary", 4.0f)
                        .field("jobTitle", 3.0f)
                        .field("department")
                        .field("name")
        );
        if (intent.getFreeText() != null && !intent.getFreeText().isBlank()) {
            bool.minimumShouldMatch(1);
        }
    }

    private void applyProjectCount(SearchIntent intent, BoolQueryBuilder bool){
        if(intent.getActiveProjectCount() == null || intent.getProjectCountcomparisonType() == null) {
            return;
        }
        RangeQueryBuilder range = QueryBuilders.rangeQuery("activeProjectCount");
        int count = intent.getActiveProjectCount();

        switch (intent.getProjectCountcomparisonType()) {
            case EQUAL -> range.gte(count).lte(count);
            case LESS_THAN -> range.lt(count);
            case LESS_THAN_OR_EQUAL -> range.lte(count);
            case GREATER_THAN -> range.gt(count);
            case GREATER_THAN_OR_EQUAL -> range.gte(count);
        }
        bool.filter(range);
    }

    private void applySeniorityRange(SearchIntent intent, BoolQueryBuilder bool) {
        SeniorityRange range = intent.getSeniorityRange();
        if (range == null) return;

        bool.filter(
                QueryBuilders.rangeQuery("seniorityLevelLevel")
                        .gte(range.getMinLevel().level())
                        .lte(range.getMaxLevel().level())
        );
    }
    private void applyJobGradeRange(SearchIntent intent, BoolQueryBuilder bool) {
        JobGradeRange range = intent.getJobGradeRange();
        if (range == null) return;

        bool.filter(
                QueryBuilders.rangeQuery("jobGradeLevel")
                        .gte(range.getMinGrade().getLevel())
                        .lte(range.getMaxGrade().getLevel())
        );
    }

    private void applySkillConditions(
            SearchIntent intent,
            BoolQueryBuilder bool
    ) {
        if (intent.getSkillConditions() == null ||
                intent.getSkillConditions().isEmpty()) {
            return;
        }

        BoolQueryBuilder skillQuery = QueryBuilders.boolQuery();

        // 전부 AND
        for (SkillCondition sc : intent.getSkillConditions()) {
            skillQuery.must(buildSkillQuery(sc));
        }

        bool.filter(skillQuery);
    }

    //단일 기술 조건을 OpenSearch Query로 변환

    private QueryBuilder buildSkillQuery(SkillCondition sc) {

        String tech = resolveIndexTechKey(sc.getTechName());

        int level = sc.getSkillLevel().number();

        return switch (sc.getComparisonType()) {
            case GREATER_THAN_OR_EQUAL ->
                    QueryBuilders.rangeQuery("techSkillNumericLevels." + tech)
                            .gte(level);

            case GREATER_THAN ->
                    QueryBuilders.rangeQuery("techSkillNumericLevels." + tech)
                            .gt(level);

            case LESS_THAN_OR_EQUAL ->
                    QueryBuilders.rangeQuery("techSkillNumericLevels." + tech)
                            .lte(level);

            case LESS_THAN ->
                    QueryBuilders.rangeQuery("techSkillNumericLevels." + tech)
                            .lt(level);

            case EQUAL ->
                    QueryBuilders.termQuery(
                            "techSkills." + tech,
                            sc.getSkillLevel().name()
                    );
        };
    }
    private String resolveIndexTechKey(String techName) {
        if (techName == null) return null;
        return TECH_KEY_TO_INDEX_KEY.getOrDefault(techName, techName);
    }

    // limit 처리
    private int resolveLimit(SearchIntent intent) {
        //사용자가 결과 개수를 지정했으면 그 값을 쓰고 지정하지 않으면 기본 10개 값 반환하기
        return intent.getLimit() != null ? intent.getLimit() : 10;
    }
    private List<PersonDocument> executeSearch(SearchSourceBuilder source) {
        try {
            System.out.println("🔥 QUERY = " + source.toString());
            SearchRequest request = new SearchRequest("people_index");
            request.source(source);

            SearchResponse response = client.search(
                    request,
                    RequestOptions.DEFAULT
            );

            return Arrays.stream(response.getHits().getHits())
                    .map(this::toPersonDocument)
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("OpenSearch search failed", e);
        }
    }

    private PersonDocument toPersonDocument(SearchHit hit) {

        Map<String, Object> source = hit.getSourceAsMap();

        return PersonDocument.builder()
                .personId(Long.valueOf(source.get("personId").toString()))
                .name((String) source.get("name"))
                .jobTitle((String) source.get("jobTitle"))
                .department((String) source.get("department"))
                .activeProjectCount(
                        ((Number) source.get("activeProjectCount")).intValue()
                )
                .seniorityLevelLevel(
                        source.get("seniorityLevelLevel") != null
                                ? ((Number) source.get("seniorityLevelLevel")).intValue()
                                : null
                )
                .jobGradeLevel(
                        source.get("jobGradeLevel") != null
                                ? ((Number) source.get("jobGradeLevel")).intValue()
                                : null
                )
                .profileSummary(
                        (String) source.get("profileSummary")
                )
                .techSkills(
                        parseTechSkills(source)
                )
                .experienceDomainText(
                        (String) source.get("experienceDomainText")
                )
                .build();
    }

    /**
     * OpenSearch 응답으로부터 techSkills를 안전하게 변환한다.
     * <p>
     * OpenSearch에서는 Map<String, String> 형태로 반환되지만,
     * PersonDocument에서는 Map<String, SkillLevel>을 사용하므로
     * 문자열 숙련도를 enum으로 변환하는 보조 로직이 필요하다.
     * <p>
     * - JSON 역직렬화 안정성 확보
     * - Enum 타입 안전성 보장
     */
    @SuppressWarnings("unchecked")
    private Map<String, SkillLevel> parseTechSkills(Map<String, Object> source) {
        Map<String, String> raw =
                (Map<String, String>) source.get("techSkills");

        if (raw == null) {
            return Map.of();
        }

        return raw.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> SkillLevel.valueOf(e.getValue())
                ));
    }
}
