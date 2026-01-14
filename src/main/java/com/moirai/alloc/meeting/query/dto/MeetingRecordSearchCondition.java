package com.moirai.alloc.meeting.query.dto;

import java.time.LocalDate;

public record MeetingRecordSearchCondition(
        String projectName,
        LocalDate from,
        LocalDate to
) {
}
