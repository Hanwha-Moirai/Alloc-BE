package com.moirai.alloc.notification.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnProperty(name = "notification.port.mode", havingValue = "http")
@EnableConfigurationProperties(NotificationHttpProperties.class)
public class NotificationHttpClientConfig {

    @Bean
    WebClient notificationWebClient(NotificationHttpProperties props) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .build();
    }
}
