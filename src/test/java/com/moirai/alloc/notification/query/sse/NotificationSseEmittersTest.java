package com.moirai.alloc.notification.query.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationSseEmittersTest {

    @Test
    @DisplayName("watchdog 기준을 넘긴 연결은 purgeStaleConnections에서 정리된다")
    void purgeStaleConnections_removesOldConnections() throws Exception {
        NotificationSseHeartbeatProperties props = new NotificationSseHeartbeatProperties();
        props.setConnectionTimeoutMs(5_000);
        props.setWatchdogStaleThresholdMs(20);

        NotificationSseEmitters emitters = new NotificationSseEmitters(props);
        emitters.add(1L);

        Thread.sleep(40);

        int purged = emitters.purgeStaleConnections();

        assertThat(purged).isEqualTo(1);
    }

    @Test
    @DisplayName("watchdog 비활성 설정(임계 <= 0)이면 purge 대상이 없다")
    void purgeStaleConnections_noopWhenDisabled() {
        NotificationSseHeartbeatProperties props = new NotificationSseHeartbeatProperties();
        props.setWatchdogStaleThresholdMs(0);

        NotificationSseEmitters emitters = new NotificationSseEmitters(props);
        emitters.add(1L);

        int purged = emitters.purgeStaleConnections();

        assertThat(purged).isZero();
    }
}