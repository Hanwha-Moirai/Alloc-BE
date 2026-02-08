package com.moirai.alloc.notification.query.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationPollResponse {

    private final List<NotificationSummaryResponse> notifications;
    private final long unreadCount;
    private final long latestNotificationId;
}
