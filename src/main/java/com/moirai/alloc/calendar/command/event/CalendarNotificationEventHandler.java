package com.moirai.alloc.calendar.command.event;

import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.TargetType;
import com.moirai.alloc.notification.common.port.NotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CalendarNotificationEventHandler {

    private final NotificationPort notificationPort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onScheduleNotification(CalendarScheduleNotificationEvent event) {
        if (event.targetUserIds() == null || event.targetUserIds().isEmpty()) {
            return;
        }
        InternalNotificationCommand command = InternalNotificationCommand.builder()
                .templateType(event.templateType())
                .targetUserIds(event.targetUserIds())
                .variables(Map.of("eventName", event.eventName()))
                .targetType(TargetType.CALENDAR)
                .targetId(event.eventId())
                .linkUrl("/projects/" + event.projectId() + "/calendar")
                .build();
        notificationPort.notify(command);
    }
}