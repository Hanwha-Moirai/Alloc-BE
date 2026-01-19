package com.moirai.alloc.notification.query.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "notification.sse.heartbeat.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationSseHeartbeatScheduler {

    private final NotificationSseEmitters emitters;
    private final NotificationSseHeartbeatProperties props;

    /**
     * SSE heartbeat 주기 전송
     * - 유휴 연결 종료 방지/프록시 타임아웃 대응
     * - 예외가 발생해도 스케줄러 스레드가 죽지 않도록 보호
     */
    @Scheduled(
            fixedDelayString = "${notification.sse.heartbeat.fixed-delay-ms:25000}",
            initialDelayString = "${notification.sse.heartbeat.initial-delay-ms:5000}"
    )
    public void heartbeat() {
        try {
            int touched = emitters.broadcastHeartbeat(props.getEventName(), props.getData());
            log.debug("SSE heartbeat sent. emittersTouched={}", touched);
        } catch (Exception e) {
            log.warn("SSE heartbeat scheduler failed.", e);
        }
    }
}
