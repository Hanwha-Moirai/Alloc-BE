package com.moirai.alloc.meeting.query.dto;

public record ParticipantResponse(
        Long userId,
        Boolean isHost
) {
}
