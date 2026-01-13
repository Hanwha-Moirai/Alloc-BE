package com.moirai.alloc.report.command.dto.response;

public record WeeklyReportDeleteResponse(
        Long reportId,
        Boolean isDeleted
) {
}
