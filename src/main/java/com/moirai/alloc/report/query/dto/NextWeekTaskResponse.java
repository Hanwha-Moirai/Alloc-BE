package com.moirai.alloc.report.query.dto;

import java.time.LocalDate;

public record NextWeekTaskResponse(
        Long taskId,
        String taskName,
        String assigneeName,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate
) {
}
