package com.moirai.alloc.notification.common.port;

import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;

public interface NotificationPort {
    InternalNotificationCreateResponse notify(InternalNotificationCommand cmd);
}
