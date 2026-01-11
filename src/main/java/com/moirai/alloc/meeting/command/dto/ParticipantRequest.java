package com.moirai.alloc.meeting.command.dto;

public record ParticipantRequest(
        Long userId,
        Boolean isHost
) {
}
