package com.moirai.alloc.notification.command.port;

import com.moirai.alloc.notification.common.port.NotificationPort;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.command.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "notification.port.mode",
        havingValue = "local",
        matchIfMissing = true
)
public class LocalNotificationPort implements NotificationPort {

    private final NotificationCommandService service;

    @Override
    public InternalNotificationCreateResponse notify(InternalNotificationCommand cmd) {
        return service.createInternalNotifications(cmd);
    }
}
