package com.moirai.alloc.notification.sqs;

import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.common.port.NotificationPort;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.port.mode", havingValue = "sqs")
public class SqsNotificationPort implements NotificationPort {

    private final SqsTemplate sqsTemplate;
    private final NotificationSqsProperties properties;

    @Override
    public InternalNotificationCreateResponse notify(InternalNotificationCommand cmd) {
        sqsTemplate.send(properties.getQueueName(), new SqsNotificationMessage(cmd));
        return InternalNotificationCreateResponse.builder()
                .createdCount(0)
                .alarmIds(List.of())
                .build();
    }
}