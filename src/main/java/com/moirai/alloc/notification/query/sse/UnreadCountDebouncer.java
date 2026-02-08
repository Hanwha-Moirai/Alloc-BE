package com.moirai.alloc.notification.query.sse;

import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnreadCountDebouncer {

    private final AlarmLogRepository alarmLogRepository;
    private final NotificationSseEmitters emitters;

    /**
     * 디바운스 윈도우: 같은 userId에 이벤트가 몰리면 이 시간 동안 묶어서 1회만 count 조회/전송
     * - 너무 길면 UI 반영이 늦고
     * - 너무 짧으면 DB 절감 효과가 줄어듦
     */
    private static final long DEBOUNCE_MS = 200;

    /**
     * userId별로 "예약된 flush 작업"을 관리
     */
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduled = new ConcurrentHashMap<>();

    /**
     * flush 대상 userId 집합(스케줄이 실행될 때 기준으로 snapshot 조회)
     */
    private final Set<Long> pendingUserIds = ConcurrentHashMap.newKeySet();

    /**
     * 가벼운 ScheduledExecutor (count 조회/전송 자체는 @Async 쓰는 곳에서 실행되므로 여기선 스케줄만)
     *
     */
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1, r -> {
                Thread t = new Thread(r);
                t.setName("notif-debounce");
                t.setDaemon(true);
                return t;
            });

    /**
     * unread count 갱신 요청을 등록
     * - 같은 userId는 디바운스 윈도우 안에서 여러 번 호출돼도 최종 1회만 처리
     */
    public void requestFlush(Long userId) {
        pendingUserIds.add(userId);

        scheduled.compute(userId, (uid, prev) -> {
            if (prev != null && !prev.isDone()) {
                prev.cancel(false);
            }
            return scheduler.schedule(() -> flushOne(uid), DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        });
    }

    private void flushOne(Long userId) {
        // pending에서 제거(없어도 idempotent)
        pendingUserIds.remove(userId);
        scheduled.remove(userId);

        try {
            long unread = alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
            emitters.sendToUser(userId, "UNREAD_COUNT", unread);
        } catch (Exception e) {
            // 최소한 로그 남기기
            log.warn("Failed to flush unread count. userId={}", userId, e);
        }
    }

    /**
     * graceful shutdown 필요 시 호출
     */
    public void shutdown(Duration await) {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(await.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
