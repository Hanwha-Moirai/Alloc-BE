package com.moirai.alloc.report.command.dto;

public record IncompleteTaskRequest(
        Long taskId,
        String delayReason
) {
}
