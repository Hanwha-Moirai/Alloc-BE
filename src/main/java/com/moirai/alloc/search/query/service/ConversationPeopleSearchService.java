package com.moirai.alloc.search.query.service;

import com.moirai.alloc.search.query.domain.condition.SearchCondition;
import com.moirai.alloc.search.query.domain.intent.SearchIntent;
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
public class ConversationPeopleSearchService {
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
        return SearchCondition.of(
                intent.getFreeText(),
                intent.getSkillConditions() != null
                        ? intent.getSkillConditions()
                        : List.of(),
                intent.getActiveProjectCount(),
                intent.getComparisonType(),
                intent.getSeniorityRange(),
                intent.getJobGradeRange(),
                intent.getDepartment(),
                intent.getLimit()
        );
    }



}
