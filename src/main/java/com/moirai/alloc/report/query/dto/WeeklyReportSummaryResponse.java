package com.moirai.alloc.report.query.dto;

import com.moirai.alloc.report.command.domain.entity.WeeklyReport;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeeklyReportSummaryResponse(
        Long reportId,
        Long projectId,
        String projectName,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        WeeklyReport.ReportStatus reportStatus,
        Double taskCompletionRate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
