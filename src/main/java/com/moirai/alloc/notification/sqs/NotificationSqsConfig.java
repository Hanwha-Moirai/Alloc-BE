package com.moirai.alloc.notification.sqs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "notification.port.mode", havingValue = "sqs")
@EnableConfigurationProperties(NotificationSqsProperties.class)
public class NotificationSqsConfig {
}