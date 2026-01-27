package com.moirai.alloc.search.query.service;

import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.infra.gpt.client.GptClient;
import com.moirai.alloc.search.query.infra.gpt.intent.ConversationContext;
import com.moirai.alloc.search.query.infra.gpt.intent.ConversationIntentParser;
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

    private final ConversationIntentParser intentParser;
    private final OpenSearchPersonSearcher searcher;
    private final PersonViewMapper mapper;
    private final GptClient gptClient;
    private final ConversationContext context;

    public ConversationSearchResponse search(
            String conversationId,
            String nl
    ) {
        SearchIntent intent =
                intentParser.parse(conversationId, nl);

        if (intent.getSeniorityRange() == null) {
            return ConversationSearchResponse.question("주니어, 미들, 시니어 중 어떤 수준을 원하시나요?");
        }

        if (intent.getExperienceDomain() == null) {
            return ConversationSearchResponse.question("어떤 프로젝트 도메인 경험이 필요한가요?");
        }

        if (intent.getSkillConditions() == null || intent.getSkillConditions().isEmpty()) {
            return ConversationSearchResponse.question("필요한 기술 스택이 있나요?");
        }

        List<PersonDocument> result = searcher.search(intent);

        if (result.isEmpty()) {
            List<PersonDocument> last =
                    context.getLastResult(conversationId);

            if (last != null) {
                return ConversationSearchResponse.result(
                        mapper.toViews(last)
                );
            }
        }

        context.saveResult(conversationId, result);

        return ConversationSearchResponse.result(
                mapper.toViews(result)
        );
    }

}
