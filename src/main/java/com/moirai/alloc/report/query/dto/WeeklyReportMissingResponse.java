package com.moirai.alloc.report.query.dto;

import java.time.LocalDate;

public record WeeklyReportMissingResponse(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        String weekLabel
) {
}
