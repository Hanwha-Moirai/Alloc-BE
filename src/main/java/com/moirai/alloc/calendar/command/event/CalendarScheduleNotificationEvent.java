package com.moirai.alloc.calendar.command.event;

import com.moirai.alloc.notification.common.contract.AlarmTemplateType;

import java.util.List;

public record CalendarScheduleNotificationEvent(
        Long projectId,
        Long eventId,
        String eventName,
        List<Long> targetUserIds,
        AlarmTemplateType templateType
) {
}