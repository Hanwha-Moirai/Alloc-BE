package com.moirai.alloc.meeting.query.dto;

import java.time.LocalDateTime;

public record MeetingRecordSummaryResponse(
        Long meetingId,
        Long projectId,
        String createdBy,
        Double progress,
        LocalDateTime meetingDate,
        LocalDateTime meetingTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
