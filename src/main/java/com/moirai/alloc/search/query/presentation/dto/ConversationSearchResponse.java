package com.moirai.alloc.search.query.presentation.dto;

import java.util.List;

public class ConversationSearchResponse {

    private ResponseType type;   // RESULT or QUESTION
    private List<PersonView> people;
    private String question;     //  질문 문장

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

    public static ConversationSearchResponse question(String question) {
        ConversationSearchResponse r = new ConversationSearchResponse();
        r.type = ResponseType.QUESTION;
        r.question = question;
        return r;
    }

    // ===== getters =====

    public ResponseType getType() {
        return type;
    }

    public List<PersonView> getPeople() {
        return people;
    }

    public String getQuestion() {
        return question;
    }
}
