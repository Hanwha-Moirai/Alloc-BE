package com.moirai.alloc.notification.query.sse;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notification.sse")
public class NotificationSseProperties {

    /**
     * emitter timeout (ms)
     */
    private long timeoutMs = 60L * 60 * 1000; // 1h

    /**
     * userId 당 최대 emitter 개수 (탭/디바이스 폭증 방지)
     */
    private int maxEmittersPerUser = 3;

    /**
     * 서버 전체 최대 emitter 개수 (보수적으로 설정, 초과 시 신규 연결 거절)
     */
    private int maxTotalEmitters = 50_000;

    private final Heartbeat heartbeat = new Heartbeat();

    @Getter
    @Setter
    public static class Heartbeat {
        private boolean enabled = true;
        private long fixedDelayMs = 25_000;
        private long initialDelayMs = 5_000;
        private String eventName = "PING";
        private String data = "ok";

        /**
         * 한 번에 전송할 emitter 배치 크기(너무 크면 스파이크, 너무 작으면 느림)
         */
        private int batchSize = 1000;
    }
}
