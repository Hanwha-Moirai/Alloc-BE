package com.moirai.alloc.search.query.service.policy;

import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import org.springframework.stereotype.Component;

@Component
public class ConversationSearchFlowPolicy {

    public ClarificationType decide(SearchIntent intent) {

        if (intent.getSeniorityRange() == null) {
            return ClarificationType.SENIORITY;
        }

        return null;
    }
}
