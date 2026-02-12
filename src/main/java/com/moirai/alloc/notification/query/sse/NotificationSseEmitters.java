package com.moirai.alloc.notification.query.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSseEmitters {

    private static final String INIT_EVENT = "INIT";

    private final NotificationSseHeartbeatProperties props;

    /** userId → (해당 유저의 다중 연결 emitter set) */
    private final Map<Long, Set<EmitterConnection>> emittersByUser = new ConcurrentHashMap<>();

    /**
     * 유저 SSE 연결 등록
     * - 다중 탭/다중 디바이스를 고려해 Set으로 관리
     * - completion/timeout/error 콜백에서 자동 제거
     * - 최초 INIT 이벤트 1회 전송(클라이언트 연결 확인 + 재연결 정책 힌트)
     */
    public SseEmitter add(Long userId) {
        SseEmitter emitter = new SseEmitter(props.getConnectionTimeoutMs());
        EmitterConnection connection = new EmitterConnection(emitter);

        emittersByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(connection);

        emitter.onCompletion(() -> remove(userId, connection));
        emitter.onTimeout(() -> remove(userId, connection));
        emitter.onError(e -> remove(userId, connection));

        try {
            connection.send(
                    SseEmitter.event()
                            .name(INIT_EVENT)
                            .data("ok", MediaType.TEXT_PLAIN)
                            .reconnectTime(props.getReconnectBaseDelayMs())
            );
        } catch (Exception e) {
            remove(userId, connection);
        }

        return emitter;
    }

    /**
     * 특정 유저에게 SSE 이벤트 전송
     * - 전송 실패 emitter는 dead로 간주하고 제거(리소스 누수 방지)
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        Set<EmitterConnection> set = emittersByUser.getOrDefault(userId, Set.of());
        List<EmitterConnection> dead = new ArrayList<>();

        for (EmitterConnection connection : set) {
            try {
                connection.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                dead.add(connection);
            }
        }

        dead.forEach(connection -> remove(userId, connection));
    }

    /**
     * 전체 emitter에 heartbeat 전송
     * - 프록시/로드밸런서 환경에서 유휴 연결 종료를 막기 위한 keep-alive 성격
     * - 각 heartbeat 응답에 reconnectTime을 포함해 클라이언트 재연결 폭주를 완화
     */
    public int broadcastHeartbeat(String eventName, Object data) {
        int touched = 0;

        Map<Long, Set<EmitterConnection>> snapshot = new HashMap<>(emittersByUser);

        for (Map.Entry<Long, Set<EmitterConnection>> entry : snapshot.entrySet()) {
            Long userId = entry.getKey();
            Set<EmitterConnection> set = entry.getValue();
            if (set == null || set.isEmpty()) {
                continue;
            }

            List<EmitterConnection> dead = new ArrayList<>();
            for (EmitterConnection connection : new ArrayList<>(set)) {
                try {
                    connection.send(
                            SseEmitter.event()
                                    .name(eventName)
                                    .data(data)
                                    .reconnectTime(connection.reconnectDelayMs(props))
                    );
                    touched++;
                } catch (Exception e) {
                    dead.add(connection);
                }
            }

            dead.forEach(connection -> remove(userId, connection));
        }

        return touched;
    }

    /**
     * Ping watchdog
     * - heartbeat 전송 성공 시각이 일정 시간 이상 갱신되지 않은 연결을 정리
     * - 조용한 단절(silent drop)로 남는 좀비 연결을 완화
     */
    public int purgeStaleConnections() {
        long staleThresholdMs = props.getWatchdogStaleThresholdMs();
        if (staleThresholdMs <= 0) {
            return 0;
        }

        long now = Instant.now().toEpochMilli();
        int purged = 0;

        for (Map.Entry<Long, Set<EmitterConnection>> entry : new HashMap<>(emittersByUser).entrySet()) {
            Long userId = entry.getKey();
            for (EmitterConnection connection : new ArrayList<>(entry.getValue())) {
                if (now - connection.lastSuccessAt() <= staleThresholdMs) {
                    continue;
                }
                remove(userId, connection);
                connection.complete();
                purged++;
            }
        }

        if (purged > 0) {
            log.info("SSE stale emitters purged. count={}", purged);
        }

        return purged;
    }

    /** emitter 제거(유저의 set이 비면 map에서도 제거) */
    private void remove(Long userId, EmitterConnection connection) {
        Set<EmitterConnection> set = emittersByUser.get(userId);
        if (set != null) {
            set.remove(connection);
            if (set.isEmpty()) {
                emittersByUser.remove(userId);
            }
        }
    }

    private static final class EmitterConnection {
        private final SseEmitter emitter;
        private final AtomicLong continuousFailures = new AtomicLong(0);
        private final AtomicLong lastSuccessAt = new AtomicLong(Instant.now().toEpochMilli());

        private EmitterConnection(SseEmitter emitter) {
            this.emitter = emitter;
        }

        private void send(SseEmitter.SseEventBuilder event) throws IOException {
            emitter.send(event);
            continuousFailures.set(0);
            lastSuccessAt.set(Instant.now().toEpochMilli());
        }

        private long reconnectDelayMs(NotificationSseHeartbeatProperties props) {
            long failures = continuousFailures.getAndIncrement();
            long base = Math.max(1L, props.getReconnectBaseDelayMs());
            long max = Math.max(base, props.getReconnectMaxDelayMs());

            long scaled;
            if (failures >= 31) {
                scaled = max;
            } else {
                scaled = base << failures;
            }
            return Math.min(max, scaled);
        }

        private long lastSuccessAt() {
            return lastSuccessAt.get();
        }

        private void complete() {
            emitter.complete();
        }
    }
}
