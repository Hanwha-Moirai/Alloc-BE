package com.moirai.alloc.notification.query.sse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationSseEmitters {

    /** SSE 연결 타임아웃(1시간). 주기적 heartbeat로 중간 끊김 완화 */
    private static final long TIMEOUT_MS = 60L * 60 * 1000;

    /** userId → (해당 유저의 다중 연결 emitter set) */
    private final Map<Long, Set<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    /**
     * 유저 SSE 연결 등록
     * - 다중 탭/다중 디바이스를 고려해 Set으로 관리
     * - completion/timeout/error 콜백에서 자동 제거
     * - 최초 INIT 이벤트 1회 전송(클라이언트 연결 확인 용도)
     */
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

    /**
     * 특정 유저에게 SSE 이벤트 전송
     * - 전송 실패 emitter는 dead로 간주하고 제거(리소스 누수 방지)
     */
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
     * 전체 emitter에 heartbeat 전송
     * - 프록시/로드밸런서 환경에서 유휴 연결 종료를 막기 위한 keep-alive 성격
     * - touched: 이번 실행에서 실제 전송 시도한 emitter 수(운영 지표)
     */
    public int broadcastHeartbeat(String eventName, Object data) {
        int touched = 0;

        Map<Long, Set<SseEmitter>> snapshot = new HashMap<>(emittersByUser);

        for (Map.Entry<Long, Set<SseEmitter>> entry : snapshot.entrySet()) {
            Long userId = entry.getKey();
            Set<SseEmitter> set = entry.getValue();
            if (set == null || set.isEmpty()) continue;

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

            dead.forEach(em -> remove(userId, em));
        }

        return touched;
    }

    /**
     * emitter 제거(유저의 set이 비면 map에서도 제거)
     */
    private void remove(Long userId, SseEmitter emitter) {
        Set<SseEmitter> set = emittersByUser.get(userId);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty()) emittersByUser.remove(userId);
        }
    }
}
