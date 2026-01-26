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
    private List<PersonDocument> lastResult;

    public List<PersonView> search(String nl) {
        //자연어-> 구조화된 intent(gpt)
        SearchIntent intent = searchIntentParser.parse(nl);

        // intent -> 검색 조건으로
        SearchCondition condition = toCondition(intent);

        // 검색 실행
        List<PersonDocument> result = searcher.search(condition);

        // 결과가 없으면 이전 결과 유지
        if (result.isEmpty() && lastResult != null) {
            return personViewMapper.toViews(lastResult);
        }

        this.lastResult = result;

        return personViewMapper.toViews(result);
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
