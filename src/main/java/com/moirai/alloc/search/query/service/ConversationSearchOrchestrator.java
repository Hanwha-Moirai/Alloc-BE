package com.moirai.alloc.search.query.service;

import com.moirai.alloc.search.command.infra.embedding.EmbeddingGenerator;
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


    private final ConversationIntentParser intentParser;
    private final OpenSearchPersonSearcher searcher;
    private final PersonViewMapper mapper;
    private final EmbeddingGenerator embeddingGenerator;

    public ConversationSearchResponse search(String conversationId, String nl) {

        SearchIntent intent = intentParser.parse(conversationId, nl);

        // fallback: semanticQuery 없으면 freeText 사용
        String semanticQuery = intent.getFreeText();

        if (semanticQuery != null && !semanticQuery.isBlank()) {
            float[] embedding = embeddingGenerator.generate(semanticQuery);
            intent.setQueryEmbedding(embedding);
        }


        //  검색
        List<PersonDocument> result = searcher.search(intent);

        return ConversationSearchResponse.result(mapper.toViews(result));
    }

}