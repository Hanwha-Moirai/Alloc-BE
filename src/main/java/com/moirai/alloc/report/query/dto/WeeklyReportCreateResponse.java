package com.moirai.alloc.report.query.dto;

import com.moirai.alloc.report.command.domain.entity.WeeklyReport;

import java.time.LocalDate;
import java.util.List;

public record WeeklyReportCreateResponse(
        Long reportId,
        Long projectId,
        String projectName,
        String reporterName,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        String weekLabel,
        WeeklyReport.ReportStatus reportStatus,
        Double taskCompletionRate,
        String summaryText,
        List<CompletedTaskResponse> completedTasks,
        List<IncompleteTaskResponse> incompleteTasks,
        List<NextWeekTaskResponse> nextWeekTasks
) {
}
