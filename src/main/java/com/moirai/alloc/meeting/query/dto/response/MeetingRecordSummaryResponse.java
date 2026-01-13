package com.moirai.alloc.meeting.query.dto.response;

import java.time.LocalDateTime;

public record MeetingRecordSummaryResponse(
        Long meetingId,
        Long projectId,
        String projectName,
        String createdBy,
        Double progress,
        LocalDateTime meetingDate,
        LocalDateTime meetingTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
