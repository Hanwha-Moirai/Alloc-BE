package com.moirai.alloc.notification.query.sse;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notification.sse.heartbeat")
public class NotificationSseHeartbeatProperties {

    /**
     * heartbeat 활성화 여부
     */
    private boolean enabled = true;

    /**
     * fixedDelay(ms): 작업 끝난 후 다음 실행까지 대기 시간
     */
    private long fixedDelayMs = 25_000;

    /**
     * initialDelay(ms): 애플리케이션 기동 후 첫 실행까지 대기 시간
     */
    private long initialDelayMs = 5_000;

    /**
     * SSE event name
     */
    private String eventName = "PING";

    /**
     * SSE payload
     */
    private String data = "ok";
}
