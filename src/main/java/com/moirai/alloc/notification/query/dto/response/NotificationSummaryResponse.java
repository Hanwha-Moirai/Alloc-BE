package com.moirai.alloc.notification.query.dto.response;

import com.moirai.alloc.notification.command.domain.entity.AlarmLog;
import com.moirai.alloc.notification.common.contract.TargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationSummaryResponse {

    private final Long notificationId;
    private final String title;
    private final String content;
    private final boolean read;
    private final LocalDateTime createdAt;

    private final TargetType targetType;
    private final Long targetId;
    private final String linkUrl;

    public static NotificationSummaryResponse from(AlarmLog alarm) {
        return NotificationSummaryResponse.builder()
                .notificationId(alarm.getId())
                .title(alarm.getAlarmTitle())
                .content(alarm.getAlarmContext())
                .read(alarm.isRead())
                .createdAt(alarm.getCreatedAt())
                .targetType(alarm.getTargetType())
                .targetId(alarm.getTargetId())
                .linkUrl(alarm.getLinkUrl())
                .build();
    }
}
