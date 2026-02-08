package com.moirai.alloc.notification.query.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class NotificationSseEmitters {

    private final NotificationSseProperties props;

    /** userId → emitter set */
    private final Map<Long, Set<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    /** 전체 emitter 개수(빠른 상한 체크용) */
    private final AtomicInteger totalEmitters = new AtomicInteger(0);

    /**
     * 유저 SSE 연결 등록
     * - 콜백에서는 detach만 수행(complete 재진입/중복 호출 위험 제거)
     * - close는 timeout/error/init-fail/send-fail 시점에만 수행
     */
    public SseEmitter add(Long userId) {

        // user set 준비
        Set<SseEmitter> set = emittersByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());

        // user별 상한(초과분은 가장 오래된/임의 하나 제거)
        enforcePerUserLimit(userId, set);

        // emitter 생성
        SseEmitter emitter = new SseEmitter(props.getTimeoutMs());

        // ---- (2) total 상한 체크 원자화 ----
        int after = totalEmitters.incrementAndGet();
        if (after > props.getMaxTotalEmitters()) {
            totalEmitters.decrementAndGet();
            // 굳이 complete 호출할 필요는 없지만 안전하게 정리
            try { emitter.complete(); } catch (Exception ignored) {}
            throw new IllegalStateException("SSE capacity exceeded (total).");
        }

        // set에 등록 (상한 통과 후)
        set.add(emitter);

        // 콜백은 detach만 (close는 재진입 위험)
        emitter.onCompletion(() -> detach(userId, emitter));
        emitter.onTimeout(() -> {
            detach(userId, emitter);
            closeQuietly(emitter);
        });
        emitter.onError(e -> {
            detach(userId, emitter);
            closeWithErrorQuietly(emitter, e);
        });

        // INIT 이벤트로 연결 유효성 확인
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("ok", MediaType.TEXT_PLAIN));
        } catch (Exception e) {
            // INIT 실패면 즉시 정리
            closeOne(userId, emitter, e);
        }

        return emitter;
    }

    /**
     * 특정 유저에게 SSE 이벤트 전송
     * - 전송 실패 emitter는 dead로 간주하고 detach + close
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        Set<SseEmitter> set = emittersByUser.get(userId);
        if (set == null || set.isEmpty()) return;

        List<SseEmitter> dead = new ArrayList<>();

        for (SseEmitter em : set) {
            try {
                em.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                dead.add(em);
                closeWithErrorQuietly(em, e);
            }
        }

        for (SseEmitter em : dead) {
            detach(userId, em);
        }
    }

    /**
     * heartbeat용: 전체 emitter snapshot을 반환 (배치 전송에 사용)
     * - 원본을 직접 노출하지 않기 위해 snapshot 리스트로 제공
     */
    public List<UserEmitter> snapshotAllEmitters() {
        List<UserEmitter> list = new ArrayList<>();
        for (Map.Entry<Long, Set<SseEmitter>> e : emittersByUser.entrySet()) {
            Long userId = e.getKey();
            Set<SseEmitter> set = e.getValue();
            if (set == null || set.isEmpty()) continue;
            for (SseEmitter em : set) {
                list.add(new UserEmitter(userId, em));
            }
        }
        return list;
    }

    /** 관측용: 연결 중인 유저 수 */
    public int connectedUsers() {
        return emittersByUser.size();
    }

    /** 관측용: 총 emitter 수 */
    public int totalEmitters() {
        return totalEmitters.get();
    }

    // --------------------
    // 핵심 추가: closeOne
    // --------------------

    /**
     * (1) 외부(heartbeat sender 등)에서 실패 emitter를 확실히 정리하기 위한 API
     * - 반드시 detach까지 수행해서 Map/Set에 남는 누수를 막는다.
     */
    public void closeOne(Long userId, SseEmitter emitter, Throwable cause) {
        detach(userId, emitter);
        closeWithErrorQuietly(emitter, cause);
    }

    // --------------------
    // internal helpers
    // --------------------

    private void enforcePerUserLimit(Long userId, Set<SseEmitter> set) {
        int limit = Math.max(1, props.getMaxEmittersPerUser());
        if (set.size() < limit) return;

        Iterator<SseEmitter> it = set.iterator();
        if (it.hasNext()) {
            SseEmitter victim = it.next();
            detach(userId, victim);
            closeQuietly(victim);
        }
    }

    /**
     * detach: Map/Set에서만 제거 (콜백에서도 안전)
     */
    private void detach(Long userId, SseEmitter emitter) {
        boolean removed = false;

        Set<SseEmitter> set = emittersByUser.get(userId);
        if (set != null) {
            removed = set.remove(emitter);
            if (set.isEmpty()) {
                emittersByUser.remove(userId, set);
            }
        }

        if (removed) {
            totalEmitters.decrementAndGet();
        }
    }

    private void closeQuietly(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    private void closeWithErrorQuietly(SseEmitter emitter, Throwable t) {
        try {
            emitter.completeWithError(t);
        } catch (Exception ignored) {
            closeQuietly(emitter);
        }
    }

    public record UserEmitter(Long userId, SseEmitter emitter) {}
}
