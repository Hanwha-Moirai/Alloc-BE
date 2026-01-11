package com.moirai.alloc.meeting.command.dto;

public record AgendaRequest(
        String discussionTitle,
        String discussionContent,
        String discussionResult,
        String agendaType
) {
}
