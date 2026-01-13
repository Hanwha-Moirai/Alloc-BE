package com.moirai.alloc.meeting.query.dto.response;

public record ParticipantResponse(
        Long userId,
        Boolean isHost
) {
}
