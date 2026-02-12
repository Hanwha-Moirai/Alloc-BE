package com.moirai.alloc.notification.query.sse;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notification.sse.heartbeat")
public class NotificationSseHeartbeatProperties {

    /** heartbeat 활성화 여부 */
    private boolean enabled = true;

    /** fixedDelay(ms): 작업 끝난 후 다음 실행까지 대기 시간 */
    private long fixedDelayMs = 25_000;

    /** initialDelay(ms): 애플리케이션 기동 후 첫 실행까지 대기 시간 */
    private long initialDelayMs = 5_000;

    /** SSE event name */
    private String eventName = "PING";

    /** SSE payload */
    private String data = "ok";

    /** 단일 SSE 연결 타임아웃(ms) */
    private long connectionTimeoutMs = 60L * 60 * 1000;

    /** 재연결 기본 지연(ms) */
    private long reconnectBaseDelayMs = 1_000;

    /** 재연결 최대 지연(ms) */
    private long reconnectMaxDelayMs = 30_000;

    /** watchdog 활성화 여부 */
    private boolean watchdogEnabled = true;

    /** 마지막 heartbeat 성공 시각 기준 좀비 연결 정리 임계(ms) */
    private long watchdogStaleThresholdMs = 120_000;

    /** watchdog 실행 주기(ms) */
    private long watchdogFixedDelayMs = 30_000;
}
