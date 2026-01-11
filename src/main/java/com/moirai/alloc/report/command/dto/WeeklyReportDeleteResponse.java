package com.moirai.alloc.report.command.dto;

public record WeeklyReportDeleteResponse(
        Long reportId,
        Boolean isDeleted
) {
}
