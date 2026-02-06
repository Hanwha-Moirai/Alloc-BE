package com.moirai.alloc.notification.sqs;

import com.moirai.alloc.notification.command.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.awspring.cloud.sqs.annotation.SqsListener;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.port.mode", havingValue = "sqs")
@Slf4j
public class NotificationSqsListener {

    private final NotificationCommandService notificationCommandService;

    @SqsListener("${notification.sqs.queue-name}")
    @Transactional
    public void onNotificationMessage(SqsNotificationMessage message) {
        if (message == null || message.command() == null) {
            log.warn("Received empty SQS notification message.");
            return;
        }
        notificationCommandService.createInternalNotifications(message.command());
    }
}