package com.moirai.alloc.meeting.command.dto.request;

public record ParticipantRequest(
        Long userId,
        Boolean isHost
) {
}
