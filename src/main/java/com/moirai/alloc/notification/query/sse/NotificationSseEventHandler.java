package com.moirai.alloc.notification.query.sse;

import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import com.moirai.alloc.notification.common.event.AlarmCreatedEvent;
import com.moirai.alloc.notification.common.event.AlarmUnreadChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;

@Component
@RequiredArgsConstructor
public class NotificationSseEventHandler {

    private final NotificationSseEmitters emitters;
    private final AlarmLogRepository alarmLogRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAlarmCreated(AlarmCreatedEvent event) {
        emitters.sendToUser(event.userId(), "NOTIFICATION", event);

        long unread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(event.userId());
        emitters.sendToUser(event.userId(), "UNREAD_COUNT", unread);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUnreadChanged(AlarmUnreadChangedEvent event) {
        long unread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(event.userId());
        emitters.sendToUser(event.userId(), "UNREAD_COUNT", unread);
    }
}
