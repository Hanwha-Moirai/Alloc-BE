package com.moirai.alloc.report.command.dto.request;

import java.time.LocalDate;

public record CreateWeeklyReportRequest(
        Long projectId,
        LocalDate weekStartDate,
        LocalDate weekEndDate
) {
}
