package com.moirai.alloc.report.query.dto;

import com.moirai.alloc.report.command.domain.entity.WeeklyReport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WeeklyReportDetailResponse(
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
        String changeOfPlan,
        List<CompletedTaskResponse> completedTasks,
        List<IncompleteTaskResponse> incompleteTasks,
        List<NextWeekTaskResponse> nextWeekTasks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
