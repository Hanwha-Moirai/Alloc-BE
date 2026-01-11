package com.moirai.alloc.meeting.query.dto;

public record AgendaResponse(
        Long agendaId,
        String discussionTitle,
        String discussionContent,
        String discussionResult,
        String agendaType
) {
}
