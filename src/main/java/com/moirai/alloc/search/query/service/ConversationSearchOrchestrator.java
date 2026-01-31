package com.moirai.alloc.search.query.service;

import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.infra.intent.ConversationContext;
import com.moirai.alloc.search.query.infra.intent.ConversationIntentParser;
import com.moirai.alloc.search.query.infra.openSearch.OpenSearchPersonSearcher;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import com.moirai.alloc.search.query.presentation.dto.ConversationSearchResponse;
import com.moirai.alloc.search.query.presentation.mapper.PersonViewMapper;
import com.moirai.alloc.search.query.service.validation.SearchIntentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationSearchOrchestrator {

    private static final int MAX_QUESTION = 2;

    private final ConversationIntentParser intentParser;
    private final OpenSearchPersonSearcher searcher;
    private final PersonViewMapper mapper;
   // private final ConversationContext context;
    private final SearchIntentValidator validator;

    public ConversationSearchResponse search(String conversationId, String nl) {

        SearchIntent intent = intentParser.parse(conversationId, nl);

//        //유효한 질문인지 판단
//        Optional<String> error = validator.validate(intent);
//        if (error.isPresent()) {
//            return ConversationSearchResponse.invalid(error.get());
//        }

//        // 질문 제한 여부 판단
//        boolean questionLimitReached =
//                context.getQuestionCount(conversationId) >= MAX_QUESTION;
//
//        // 질문 가능하면 다음 질문 시도
//        if (!questionLimitReached) {
//            String q = nextQuestion(intent);
//
//            if (q != null) {
//                context.incrementQuestionCount(conversationId);
//                return ConversationSearchResponse.question(q);
//            }
//        }

        // 더 이상 질문 안 함 → 즉시 검색
        List<PersonDocument> result = searcher.search(intent);

        // 결과 0건이면 이전 결과 fallback
//        if (result.isEmpty()) {
//            List<PersonDocument> last = context.getLastResult(conversationId);
//            if (last != null) {
//                return ConversationSearchResponse.result(mapper.toViews(last));
//            }
//        }

        //context.saveResult(conversationId, result);
        return ConversationSearchResponse.result(mapper.toViews(result));
    }

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
