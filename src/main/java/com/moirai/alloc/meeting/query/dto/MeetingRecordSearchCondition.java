package com.moirai.alloc.meeting.query.dto;

import java.time.LocalDate;

public record MeetingRecordSearchCondition(
        Long projectId,
        LocalDate from,
        LocalDate to,
        String keyword
) {
}
