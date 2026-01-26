package com.moirai.alloc.search.query.presentation.dto;

import com.moirai.alloc.search.query.service.policy.ClarificationType;
import java.util.List;

public class ConversationSearchResponse {

    private ResponseType type;   // RESULT or QUESTION
    private List<PersonView> people;
    private ClarificationType clarificationType;

    public enum ResponseType {
        RESULT,
        QUESTION
    }

    // ===== factory methods =====

    public static ConversationSearchResponse result(List<PersonView> people) {
        ConversationSearchResponse r = new ConversationSearchResponse();
        r.type = ResponseType.RESULT;
        r.people = people;
        return r;
    }

    public static ConversationSearchResponse question(ClarificationType type) {
        ConversationSearchResponse r = new ConversationSearchResponse();
        r.type = ResponseType.QUESTION;
        r.clarificationType = type;
        return r;
    }

    // ===== getters =====

    public ResponseType getType() {
        return type;
    }

    public List<PersonView> getPeople() {
        return people;
    }

    public ClarificationType getClarificationType() {
        return clarificationType;
    }
}
