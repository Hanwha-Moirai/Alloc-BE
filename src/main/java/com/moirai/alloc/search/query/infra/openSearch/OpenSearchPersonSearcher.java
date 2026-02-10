package com.moirai.alloc.search.query.infra.openSearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;

    public List<PersonDocument> search(SearchIntent intent) {
        // 1. SearchCondition 확인
        // 2. 조건별로 쿼리 조립
        // 3. OpenSearch 호출 (아직 구현 X)
        // 4. PersonDocument 리스트 반환
        BoolQueryBuilder bool = QueryBuilders.boolQuery();

        //1. 구조화된 조건 -> filter
        applyJobGradeRange(intent, bool);
        applyJobRole(intent, bool);
        applySkillConditions(intent, bool);
        applySeniorityRange(intent, bool);
        applyProjectCount(intent, bool);

        //2. 키워드 기반 검색
        if (intent.getFreeText() != null && !intent.getFreeText().isBlank()) {
            bool.should(
                    QueryBuilders.multiMatchQuery(intent.getFreeText())
                            .field("profileSummary", 3.0f)
                            .field("jobTitle", 2.0f)
                            .field("department")
                            .field("name")
            );
        }
        // 3. 유사도 기반 검색
        if (intent.getQueryEmbedding() != null) {
            try {

                Map<String, Object> knn = Map.of(
                        "knn", Map.of(
                                "profileEmbedding", Map.of(
                                        "vector", intent.getQueryEmbedding(),
                                        "k", 5
                                )
                        )
                );

                bool.should(
                        QueryBuilders.wrapperQuery(
                                objectMapper.writeValueAsString(knn)
                        )
                );

            } catch (Exception e) {
                throw new RuntimeException("KNN query build failed", e);
            }
        }
        bool.should(QueryBuilders.matchAllQuery().boost(0.1f));
        bool.minimumShouldMatch(0);

        SearchSourceBuilder source = new SearchSourceBuilder()
                .query(bool)
                .size(10);

        return executeSearch(source);
    }

    private static final Map<String, String> TECH_KEY_TO_INDEX_KEY = Map.ofEntries(
            Map.entry("JAVA", "JAVA"),
            Map.entry("SPRING", "SPRING_BOOT"),
            Map.entry("PYTHON", "PYTHON"),

            Map.entry("MYSQL", "MYSQL"),
            Map.entry("MARIADB", "MARIADB"),
            Map.entry("POSTGRESQL", "POSTGRESQL"),

            Map.entry("REDIS", "REDIS"),
            Map.entry("KAFKA", "KAFKA"),

            Map.entry("DOCKER", "DOCKER"),
            Map.entry("KUBERNETES", "KUBERNETES"),

            Map.entry("OPENSEARCH", "OPENSEARCH"),
            Map.entry("ELASTICSEARCH", "ELASTICSEARCH")
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
    private void applyJobRole(SearchIntent intent, BoolQueryBuilder bool) {
        if (intent.getJobRole() == null) return;

        bool.filter(
                QueryBuilders.termQuery(
                        "jobRole",
                        intent.getJobRole().name()
                )
        );
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

        bool.must(
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
        return QueryBuilders.rangeQuery(
                "techSkillNumericLevels." + tech
        ).gte(level);

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
            System.out.println("QUERY = " + source.toString());
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
                .jobRole((String) source.get("jobRole"))
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