package com.moirai.alloc.notification.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notification.http")
public class NotificationHttpProperties {
    /**
     * 예: http://notification-service (MSA) 또는 http://localhost:8080 (dev)
     */
    private String baseUrl;

    /**
     * 내부 엔드포인트 경로(기본값)
     */
    private String createPath = "/api/internal/notifications";
}
