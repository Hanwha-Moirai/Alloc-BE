package com.moirai.alloc.report.query.dto;

import com.moirai.alloc.report.command.domain.entity.WeeklyReport;

import java.time.LocalDate;

public record WeeklyReportSearchCondition(
        Long projectId,
        Long userId,
        WeeklyReport.ReportStatus reportStatus,
        LocalDate weekStartFrom,
        LocalDate weekStartTo,
        String keyword
) {
}
