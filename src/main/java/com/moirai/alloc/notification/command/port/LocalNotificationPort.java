package com.moirai.alloc.notification.command.port;

import com.moirai.alloc.notification.common.port.NotificationPort;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.command.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalNotificationPort implements NotificationPort {

    private final NotificationCommandService service;

    @Override
    public InternalNotificationCreateResponse notify(InternalNotificationCommand cmd) {
        return service.createInternalNotifications(cmd);
    }
}
