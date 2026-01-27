package com.moirai.alloc.search.query.presentation.api;

import com.moirai.alloc.search.query.presentation.dto.ConversationSearchResponse;
import com.moirai.alloc.search.query.service.ConversationSearchOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/people")
public class PeopleSearchController {
    private final ConversationSearchOrchestrator searchPeopleByNaturalLanguage;

    @PostMapping("/search")
    public ConversationSearchResponse search(
            @RequestParam String conversationId,
            @RequestBody String query
    ) {
        return searchPeopleByNaturalLanguage.search(conversationId, query);
    }

}
