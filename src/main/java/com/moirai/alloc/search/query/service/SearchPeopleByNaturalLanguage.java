package com.moirai.alloc.search.query.service;

import com.moirai.alloc.search.query.domain.model.ComparisonType;
import com.moirai.alloc.search.query.domain.model.SearchCondition;
import com.moirai.alloc.search.query.domain.model.SearchIntent;
import com.moirai.alloc.search.query.infra.gpt.SearchIntentParser;
import com.moirai.alloc.search.query.infra.openSearch.OpenSearchPersonSearcher;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
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

    public List<PersonDocument> search(String nl) {
        //자연어-> searchintent(gpt)
        SearchIntent intent = searchIntentParser.parse(nl);

        // searchintent -> searchcondition으로
        SearchCondition condition = toCondition(intent);

        //searchcondition -> opensearch 검색
        return searcher.search(condition);
    }
    private SearchCondition toCondition(SearchIntent intent) {

        return SearchCondition.builder()
                .skillLevel(intent.getSkillLevel())
                .activeProjectCount(intent.getActiveProjectCount())
                .comparisonType(
                        intent.getComparisonType() != null
                                ? intent.getComparisonType()
                                : ComparisonType.LESS_THAN_OR_EQUAL
                )
                .freeText(intent.getFreeText())
                .job(intent.getJob())
                .techs(intent.getTechs())
                .department(intent.getDepartment())
                .workingType(intent.getWorkingType())
                .build();
    }


}
