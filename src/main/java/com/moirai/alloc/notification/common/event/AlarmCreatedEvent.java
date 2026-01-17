package com.moirai.alloc.notification.common.event;

import com.moirai.alloc.notification.command.domain.entity.TargetType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AlarmCreatedEvent(
        Long userId,
        Long alarmId,
        String title,
        String content,
        TargetType targetType,
        Long targetId,
        String linkUrl,
        LocalDateTime createdAt
) {}
