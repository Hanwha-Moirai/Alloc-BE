package com.moirai.alloc.notification.sqs;

import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;

public record SqsNotificationMessage(InternalNotificationCommand command) {
}