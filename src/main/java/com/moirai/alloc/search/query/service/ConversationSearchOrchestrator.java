package com.moirai.alloc.search.query.service;

import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.infra.intent.ConversationIntentParser;
import com.moirai.alloc.search.query.infra.openSearch.OpenSearchPersonSearcher;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import com.moirai.alloc.search.query.presentation.dto.ConversationSearchResponse;
import com.moirai.alloc.search.query.presentation.mapper.PersonViewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationSearchOrchestrator {

    private static final int MAX_QUESTION = 2;

    private final ConversationIntentParser intentParser;
    private final OpenSearchPersonSearcher searcher;
    private final PersonViewMapper mapper;


    public ConversationSearchResponse search(String conversationId, String nl) {

        SearchIntent intent = intentParser.parse(conversationId, nl);

        // 더 이상 질문 안 함 → 즉시 검색
        List<PersonDocument> result = searcher.search(intent);

        return ConversationSearchResponse.result(mapper.toViews(result));
    }

    //todo; valdiater 추가
    /**
     * 질문 우선순위: 직군 → 직위(시니어리티) → 기술 → 경험
     * 질문은 최대 2번만 허용 (MAX_QUESTION)
     */
//    private String nextQuestion(SearchIntent intent) {
//
//        if (intent.getJobRole() == null) {
//            return "어떤 직군을 찾고 계신가요? (예: 백엔드, 인프라, 프론트)";
//        }
//
//        if (intent.getSeniorityRange() == null && intent.getJobGradeRange() == null) {
//            return "주니어, 미들, 시니어 중 어떤 수준을 원하시나요?";
//        }
//
//        if (intent.getSkillConditions() == null || intent.getSkillConditions().isEmpty()) {
//            return "필요한 기술 스택이 있나요? (예: Java, Python, Kubernetes)";
//        }
//
//        if (intent.getExperienceDomain() == null) {
//            return "어떤 프로젝트 도메인 경험이 필요한가요? (예: 금융, 공공, 커머스)";
//        }
//
//        return null;
//    }
}
