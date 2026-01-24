package com.moirai.alloc.search.query.service;

import com.moirai.alloc.search.query.domain.model.*;
import com.moirai.alloc.search.query.infra.gpt.SearchIntentParser;
import com.moirai.alloc.search.query.infra.openSearch.OpenSearchPersonSearcher;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import com.moirai.alloc.search.query.presentation.dto.PersonView;
import com.moirai.alloc.search.query.presentation.mapper.PersonViewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchPeopleByNaturalLanguage {
    // 사람을 자연어로 검색한다.
    //1. 자연어 -> 구조화
    //2. 검색 조건 생성
    //3. openSearch 조회
    //4. view 반환

    private final SearchIntentParser searchIntentParser;
    private final OpenSearchPersonSearcher searcher;
    private final PersonViewMapper personViewMapper;

    public List<PersonView> search(String nl) {
        //자연어-> 구조화된 intent(gpt)
        SearchIntent intent = searchIntentParser.parse(nl);

        // intent -> 검색 조건으로
        SearchCondition condition = toCondition(intent);

        //opensearch 조회
        List<PersonDocument> docs = searcher.search(condition);

        //view 반환
        return personViewMapper.toViews(docs);
    }
    private SearchCondition toCondition(SearchIntent intent) {

        // 기술 조건 변환
        List<SkillCondition> skillConditions =
                intent.getTechName() == null || intent.getTechName().isEmpty()
                        ? List.of()
                        : intent.getTechName().stream()
                        .map(tech ->
                                SkillCondition.builder()
                                        .techName(tech)
                                        .skillLevel(intent.getSkillLevel())
                                        .comparisonType(
                                                intent.getComparisonType() != null
                                                        ? intent.getComparisonType()
                                                        : ComparisonType.GREATER_THAN_OR_EQUAL
                                        )
                                        .build()
                        )
                        .toList();

        return SearchCondition.builder()
                // 자유 텍스트
                .freeText(intent.getFreeText())

                // 기술 조건
                .skillConditions(skillConditions)
                .logicalOperator(
                        skillConditions.size() > 1
                                ? LogicalOperator.AND
                                : LogicalOperator.OR
                )

                // 숫자 조건
                .activeProjectCount(intent.getActiveProjectCount())
                .comparisonType(
                        intent.getComparisonType() != null
                                ? intent.getComparisonType()
                                : ComparisonType.LESS_THAN_OR_EQUAL
                )

                // enum 필터
                .workingType(intent.getWorkingType())
                .seniorityLevel(intent.getSeniorityLevel())

                // 정확 매칭
                .jobTitle(intent.getJobTitle())
                .department(intent.getDepartment())

                // 결과 제한
                .limit(
                        intent.getLimit() != null
                                ? intent.getLimit()
                                : 20
                )
                .build();
    }


}
