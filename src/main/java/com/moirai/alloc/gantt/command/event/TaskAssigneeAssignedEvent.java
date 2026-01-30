package com.moirai.alloc.gantt.command.event;

public record TaskAssigneeAssignedEvent(
        Long projectId,
        Long taskId,
        Long assigneeId,
        String taskName
) {
}
