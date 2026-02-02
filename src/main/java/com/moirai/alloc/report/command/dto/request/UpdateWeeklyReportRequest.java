package com.moirai.alloc.report.command.dto.request;

import com.moirai.alloc.report.command.domain.entity.WeeklyReport;

import java.util.List;

public record UpdateWeeklyReportRequest(
        Long reportId,
        WeeklyReport.ReportStatus reportStatus,
        String changeOfPlan,
        List<CompletedTaskRequest> completedTasks,
        List<IncompleteTaskRequest> incompleteTasks,
        List<NextWeekTaskRequest> nextWeekTasks
) {
}
