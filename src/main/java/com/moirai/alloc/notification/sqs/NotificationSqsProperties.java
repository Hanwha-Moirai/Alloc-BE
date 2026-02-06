package com.moirai.alloc.notification.sqs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notification.sqs")
public class NotificationSqsProperties {
    /**
     * SQS queue name (or ARN) for notification events.
     */
    private String queueName;
}