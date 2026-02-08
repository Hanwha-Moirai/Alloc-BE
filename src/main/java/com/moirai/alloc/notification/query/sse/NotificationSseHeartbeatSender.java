package com.moirai.alloc.notification.query.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationSseHeartbeatSender {

    private final NotificationSseEmitters emitters;

    @Async("notificationExecutor")
    public void sendInBatches(List<NotificationSseEmitters.UserEmitter> all,
                              String eventName,
                              Object data,
                              int batchSize) {

        int n = all.size();
        for (int i = 0; i < n; i += batchSize) {
            int end = Math.min(i + batchSize, n);
            List<NotificationSseEmitters.UserEmitter> batch = all.subList(i, end);

            for (NotificationSseEmitters.UserEmitter ue : batch) {
                Long userId = ue.userId();
                SseEmitter em = ue.emitter();
                try {
                    em.send(SseEmitter.event().name(eventName).data(data));
                } catch (Exception e) {
                    // ---- (1) 실패 시 detach + close를 확실히 수행 ----
                    emitters.closeOne(userId, em, e);
                }
            }
        }
    }
}
