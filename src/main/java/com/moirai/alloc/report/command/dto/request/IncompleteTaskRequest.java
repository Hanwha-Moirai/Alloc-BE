package com.moirai.alloc.report.command.dto.request;

public record IncompleteTaskRequest(
        Long taskId,
        String delayReason
) {
}
