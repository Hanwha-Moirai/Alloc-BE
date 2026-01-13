package com.moirai.alloc.meeting.query.dto.response;

public record AgendaResponse(
        Long agendaId,
        String discussionTitle,
        String discussionContent,
        String discussionResult,
        String agendaType
) {
}
