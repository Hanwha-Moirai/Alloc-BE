package com.moirai.alloc.notification.query.sse;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(NotificationSseHeartbeatProperties.class)
public class NotificationSseSchedulingConfig {
}
