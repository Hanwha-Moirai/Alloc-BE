package com.moirai.alloc.gantt.command.event;

public record MilestoneCreatedEvent(
        Long projectId,
        Long milestoneId,
        String milestoneName
) {
}
