package com.moirai.alloc.notification.query.sse;

import com.moirai.alloc.notification.common.event.AlarmCreatedEvent;
import com.moirai.alloc.notification.common.event.AlarmUnreadChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSseEventHandler {

    private final NotificationSseEmitters emitters;
    private final UnreadCountDebouncer unreadCountDebouncer;

    /**
     * 알림 생성 이벤트(커밋 이후)
     * - (1) NOTIFICATION 전송
     * - (2) UNREAD_COUNT는 userId 단위로 디바운스/집계하여 1회만 전송
     * 주의:
     * - UNREAD_COUNT를 항상 즉시 원하면 디바운스 시간을 더 낮추거나 0으로 조정
     */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAlarmCreated(AlarmCreatedEvent event) {
        try {
            // NOTIFICATION 전송
            emitters.sendToUser(event.userId(), "NOTIFICATION", event);

            // unread count는 디바운스
            unreadCountDebouncer.requestFlush(event.userId());
        } catch (Exception e) {
            log.warn("Failed to handle AlarmCreatedEvent. userId={}, alarmId={}",
                    event.userId(), safeAlarmId(event), e);
        }
    }

    /**
     * 읽음/삭제 등 미읽음 상태 변경(커밋 이후)
     * - UNREAD_COUNT는 userId 단위로 디바운스/집계
     */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUnreadChanged(AlarmUnreadChangedEvent event) {
        try {
            unreadCountDebouncer.requestFlush(event.userId());
        } catch (Exception e) {
            log.warn("Failed to handle AlarmUnreadChangedEvent. userId={}", event.userId(), e);
        }
    }

    private Object safeAlarmId(AlarmCreatedEvent e) {
        try { return e.alarmId(); } catch (Exception ex) { return "n/a"; }
    }
}
