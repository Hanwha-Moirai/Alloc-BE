package com.moirai.alloc.management.command.event;

public record ProjectFinalAssignmentEvent(
        Long projectId,
        String projectName,
        Long userId
) {
}
