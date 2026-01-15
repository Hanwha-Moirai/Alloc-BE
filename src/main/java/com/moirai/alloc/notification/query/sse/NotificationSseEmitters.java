package com.moirai.alloc.notification.query.sse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationSseEmitters {

    private static final long TIMEOUT_MS = 60L * 60 * 1000; // 1시간
    private final Map<Long, Set<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    public SseEmitter add(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emittersByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("ok", MediaType.TEXT_PLAIN));
        } catch (Exception ignored) {}

        return emitter;
    }

    public void sendToUser(Long userId, String eventName, Object data) {
        Set<SseEmitter> set = emittersByUser.getOrDefault(userId, Set.of());
        List<SseEmitter> dead = new ArrayList<>();

        for (SseEmitter em : set) {
            try {
                em.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                dead.add(em);
            }
        }
        dead.forEach(em -> remove(userId, em));
    }

    /**
     * 모든 사용자 emitter에 heartbeat 이벤트 전송
     * @return 이번 heartbeat에서 "접근한 emitter 수"(대략적인 지표)
     */
    public int broadcastHeartbeat(String eventName, Object data) {
        int touched = 0;

        // snapshot (순회 중 concurrent 변경 안전)
        Map<Long, Set<SseEmitter>> snapshot = new HashMap<>(emittersByUser);

        for (Map.Entry<Long, Set<SseEmitter>> entry : snapshot.entrySet()) {
            Long userId = entry.getKey();
            Set<SseEmitter> set = entry.getValue();
            if (set == null || set.isEmpty()) continue;

            // emitter set도 snapshot
            List<SseEmitter> emitters = new ArrayList<>(set);
            List<SseEmitter> dead = new ArrayList<>();

            for (SseEmitter em : emitters) {
                try {
                    em.send(SseEmitter.event().name(eventName).data(data));
                    touched++;
                } catch (Exception e) {
                    dead.add(em);
                }
            }

            // 죽은 emitter 제거
            dead.forEach(em -> remove(userId, em));
        }

        return touched;
    }

    private void remove(Long userId, SseEmitter emitter) {
        Set<SseEmitter> set = emittersByUser.get(userId);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty()) emittersByUser.remove(userId);
        }
    }
}
