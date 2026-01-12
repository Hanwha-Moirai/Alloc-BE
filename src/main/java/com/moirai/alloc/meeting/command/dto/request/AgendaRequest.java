package com.moirai.alloc.meeting.command.dto.request;

public record AgendaRequest(
        String discussionTitle,
        String discussionContent,
        String discussionResult,
        String agendaType
) {
}
