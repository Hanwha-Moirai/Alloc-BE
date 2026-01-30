package com.moirai.alloc.search;

import com.moirai.alloc.search.query.presentation.dto.ConversationSearchResponse;
import com.moirai.alloc.search.query.service.ConversationSearchOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class ConversationSearchController {

    private final ConversationSearchOrchestrator orchestrator;

    @PostMapping("/conversation")
    public ConversationSearchResponse search(
            @RequestParam String conversationId,
            @RequestParam String nl
    ) {
        return orchestrator.search(conversationId, nl);
    }
}
