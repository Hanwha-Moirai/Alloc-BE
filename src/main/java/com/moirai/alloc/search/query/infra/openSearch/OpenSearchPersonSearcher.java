package com.moirai.alloc.search.query.infra.openSearch;

import com.moirai.alloc.search.query.domain.condition.*;
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


    public List<PersonDocument> search(SearchCondition condition) {
        // 1. SearchCondition 확인
        // 2. 조건별로 쿼리 조립
        // 3. OpenSearch 호출 (아직 구현 X)
        // 4. PersonDocument 리스트 반환
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        applyFreeText(condition, boolQuery);
        applyProjectCount(condition, boolQuery);

        applySeniorityRange(condition, boolQuery);
        applyJobGradeRange(condition, boolQuery);
        applySkillConditions(condition, boolQuery);

        // search request 생성
        SearchSourceBuilder source = new SearchSourceBuilder()
                .query(boolQuery)
                .size(resolveLimit(condition));

        // open search 호출
        return executeSearch(source);
    }

    private void applyFreeText(SearchCondition condition, BoolQueryBuilder bool) {
        // 자유 자연어 검색, 동의어 없어도 동작
        if(condition.getFreeText() == null || condition.getFreeText().isBlank()){
            return; //해당되지 않으면 넘어가기
        }
        bool.must(
                // freeText는 score 계산 대상 → must
                // 나머지 조건은 점수에 영향 X → filter
                QueryBuilders.multiMatchQuery(
                        condition.getFreeText(),
                        "experienceDomainText^5",
                        "profileSummary^4",
                        "jobTitle^3",
                        "department",
                        "name"
                // ^5의 의미; 가중치; experience에서 걸리면 점수 가장 크게, 요약, 직무, 부서 순
                // 그 외 필드는 filter 처리할 것.
                )
        );
    }

    private void applyProjectCount(SearchCondition condition, BoolQueryBuilder bool){
        if(condition.getActiveProjectCount() == null || condition.getProjectCountComparisonType() == null) {
            return;
        }
        RangeQueryBuilder range = QueryBuilders.rangeQuery("activeProjectCount");
        int count = condition.getActiveProjectCount();
        switch (condition.getProjectCountComparisonType()) {
            case EQUAL -> {
                range.gte(count);
                range.lte(count);
            }
            case LESS_THAN -> range.lt(count);
            case LESS_THAN_OR_EQUAL -> range.lte(count);
            case GREATER_THAN -> range.gt(count);
            case GREATER_THAN_OR_EQUAL -> range.gte(count);
        }
        bool.filter(range);
    }

    private void applySeniorityRange(SearchCondition condition, BoolQueryBuilder bool) {
        SeniorityRange range = condition.getSeniorityRange();
        if (range == null) return;

        bool.filter(
                QueryBuilders.rangeQuery("seniorityLevelLevel")
                        .gte(range.getMinLevel().level())
                        .lte(range.getMaxLevel().level())
        );
    }
    private void applyJobGradeRange(SearchCondition condition, BoolQueryBuilder bool) {
        JobGradeRange range = condition.getJobGradeRange();
        if (range == null) return;

        bool.filter(
                QueryBuilders.rangeQuery("jobGradeLevel")
                        .gte(range.getMinGrade().getLevel())
                        .lte(range.getMaxGrade().getLevel())
        );
    }

    private void applySkillConditions(
            SearchCondition condition,
            BoolQueryBuilder bool
    ) {
        if (condition.getSkillConditions() == null ||
                condition.getSkillConditions().isEmpty()) {
            return;
        }

        BoolQueryBuilder skillQuery = QueryBuilders.boolQuery();

        // 전부 AND
        for (SkillCondition sc : condition.getSkillConditions()) {
            skillQuery.must(buildSkillQuery(sc));
        }

        bool.filter(skillQuery);
    }

    //단일 기술 조건을 OpenSearch Query로 변환

    private QueryBuilder buildSkillQuery(SkillCondition sc) {

        String tech = sc.getTechName();

        // LV2 이상, LV3 이상 같은 범위 검색
        if (sc.getComparisonType() == ComparisonType.GREATER_THAN_OR_EQUAL) {
            return QueryBuilders.rangeQuery(
                    "techSkillLevels." + tech
            ).gte(sc.getSkillLevel().number());
        }

        // 정확 매칭 (LV2, LV3)
        return QueryBuilders.termQuery(
                "techSkills." + tech,
                sc.getSkillLevel().name()
        );
    }


    // limit 처리
    private int resolveLimit(SearchCondition condition) {
        //사용자가 결과 개수를 지정했으면 그 값을 쓰고 지정하지 않으면 기본 10개 값 반환하기
        return condition.getLimit() != null ? condition.getLimit() : 10;
    }
    private List<PersonDocument> executeSearch(SearchSourceBuilder source) {
        try {
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
                .build();
    }

    /**
     * OpenSearch 응답으로부터 techSkills를 안전하게 변환한다.
     *
     * OpenSearch에서는 Map<String, String> 형태로 반환되지만,
     * PersonDocument에서는 Map<String, SkillLevel>을 사용하므로
     * 문자열 숙련도를 enum으로 변환하는 보조 로직이 필요하다.
     *
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