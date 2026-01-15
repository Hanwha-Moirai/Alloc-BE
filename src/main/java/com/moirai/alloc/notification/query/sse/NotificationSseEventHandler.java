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

    /**
     * 알림 생성 이벤트 수신(커밋 이후)
     * - NOTIFICATION: 알림 payload push
     * - UNREAD_COUNT: 갱신된 미읽음 개수 push (UI 뱃지 즉시 반영)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAlarmCreated(AlarmCreatedEvent event) {
        emitters.sendToUser(event.userId(), "NOTIFICATION", event);

        long unread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(event.userId());
        emitters.sendToUser(event.userId(), "UNREAD_COUNT", unread);
    }

    /**
     * 읽음/삭제 등으로 미읽음 상태가 바뀐 경우(커밋 이후)
     * - UNREAD_COUNT만 push하여 UI 카운트 동기화
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUnreadChanged(AlarmUnreadChangedEvent event) {
        long unread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(event.userId());
        emitters.sendToUser(event.userId(), "UNREAD_COUNT", unread);
    }
}
