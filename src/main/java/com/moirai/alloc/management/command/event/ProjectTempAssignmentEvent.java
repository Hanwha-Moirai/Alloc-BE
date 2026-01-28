package com.moirai.alloc.management.command.event;

public record ProjectTempAssignmentEvent(
        Long projectId,
        String projectName,
        Long userId
) {
}
