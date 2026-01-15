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
     * - fixedDelay: 이전 실행이 끝난 뒤 delay만큼 기다렸다가 실행
     * - initialDelay: 서버 기동 직후 쏠림 완화
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
            // heartbeat 자체 실패로 스케줄러가 죽지 않게 보호
            log.warn("SSE heartbeat scheduler failed.", e);
        }
    }
}
