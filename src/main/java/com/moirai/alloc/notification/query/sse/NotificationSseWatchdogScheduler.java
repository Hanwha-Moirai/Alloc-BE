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
        name = "notification.sse.heartbeat.watchdog-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationSseWatchdogScheduler {

    private final NotificationSseEmitters emitters;

    @Scheduled(
            fixedDelayString = "${notification.sse.heartbeat.watchdog-fixed-delay-ms:30000}",
            initialDelayString = "${notification.sse.heartbeat.initial-delay-ms:5000}"
    )
    public void purgeStaleConnections() {
        try {
            int purged = emitters.purgeStaleConnections();
            if (purged > 0) {
                log.info("SSE watchdog purged stale connections. count={}", purged);
            }
        } catch (Exception e) {
            log.warn("SSE watchdog scheduler failed.", e);
        }
    }
}