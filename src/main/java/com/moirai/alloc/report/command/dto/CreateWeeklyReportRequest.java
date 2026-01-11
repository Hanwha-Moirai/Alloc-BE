package com.moirai.alloc.report.command.dto;

import java.time.LocalDate;

public record CreateWeeklyReportRequest(
        Long projectId,
        LocalDate weekStartDate,
        LocalDate weekEndDate
) {
}
