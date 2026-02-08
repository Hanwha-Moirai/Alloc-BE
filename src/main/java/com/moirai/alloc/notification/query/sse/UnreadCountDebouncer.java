package com.moirai.alloc.notification.query.sse;

import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnreadCountDebouncer {

    private final AlarmLogRepository alarmLogRepository;
    private final NotificationSseEmitters emitters;

    private static final long DEBOUNCE_MS = 200;

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduled = new ConcurrentHashMap<>();

    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, r -> {
        Thread t = new Thread(r);
        t.setName("notif-debounce");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    void init() {
        // cancel된 task가 큐에 남아 메모리 증가하는 문제 방지
        scheduler.setRemoveOnCancelPolicy(true);
    }

    public void requestFlush(Long userId) {
        scheduled.compute(userId, (uid, prev) -> {
            if (prev != null && !prev.isDone()) {
                prev.cancel(false);
            }
            return scheduler.schedule(() -> flushOne(uid), DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        });
    }

    private void flushOne(Long userId) {
        scheduled.remove(userId);

        try {
            long unread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
            emitters.sendToUser(userId, "UNREAD_COUNT", unread);
        } catch (Exception e) {
            log.warn("Failed to flush unread count. userId={}", userId, e);
        }
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
