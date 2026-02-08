package com.moirai.alloc.notification.query.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final NotificationSseProperties props;
    private final NotificationSseHeartbeatSender sender;

    // ---- (3) 동시 실행 방지 가드 ----
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(
            fixedDelayString = "${notification.sse.heartbeat.fixed-delay-ms:25000}",
            initialDelayString = "${notification.sse.heartbeat.initial-delay-ms:5000}"
    )
    public void heartbeat() {
        // 이미 실행 중이면 이번 tick은 스킵
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            List<NotificationSseEmitters.UserEmitter> all = emitters.snapshotAllEmitters();
            if (all.isEmpty()) return;

            int batchSize = Math.max(100, props.getHeartbeat().getBatchSize());
            sender.sendInBatches(all, props.getHeartbeat().getEventName(), props.getHeartbeat().getData(), batchSize);

            log.debug("SSE heartbeat scheduled. totalEmitters={} connectedUsers={}",
                    emitters.totalEmitters(), emitters.connectedUsers());
        } catch (Exception e) {
            log.warn("SSE heartbeat scheduler failed.", e);
        } finally {
            // sender는 @Async라 실제 전송은 이후에 진행됨.
            // 최소 수정 기준에서는 "스케줄 enqueue 중복"만 방지하면 충분하므로 여기서 해제.
            // (더 엄격히 하려면 sender 완료 시점에 해제하도록 CompletableFuture로 바꾸면 됨)
            running.set(false);
        }
    }
}
