package com.moirai.alloc.meeting.query.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MeetingRecordDetailResponse(
        Long meetingId,
        Long projectId,
        String createdBy,
        Double progress,
        LocalDateTime meetingDate,
        LocalDateTime meetingTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AgendaResponse> agendas,
        List<ParticipantResponse> participants
) {
}
