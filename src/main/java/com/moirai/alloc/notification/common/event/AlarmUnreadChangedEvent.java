package com.moirai.alloc.notification.common.event;

import lombok.Builder;

@Builder
public record AlarmUnreadChangedEvent(Long userId) {}
