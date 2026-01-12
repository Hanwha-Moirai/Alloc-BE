package com.moirai.alloc.calendar.command.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AllocationSyncResponse {
    private int processedUsers;
    private int upsertedEvents;
}
