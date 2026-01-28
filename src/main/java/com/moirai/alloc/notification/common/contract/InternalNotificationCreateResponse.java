package com.moirai.alloc.notification.common.contract;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InternalNotificationCreateResponse {
    private final int createdCount;
    private final List<Long> alarmIds;
}
