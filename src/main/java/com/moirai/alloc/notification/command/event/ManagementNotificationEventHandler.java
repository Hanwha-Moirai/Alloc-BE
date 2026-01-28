package com.moirai.alloc.notification.command.event;

import com.moirai.alloc.management.command.event.ProjectTempAssignmentEvent;
import com.moirai.alloc.notification.common.contract.AlarmTemplateType;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.TargetType;
import com.moirai.alloc.notification.common.port.NotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ManagementNotificationEventHandler {

    private final NotificationPort notificationPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProjectTempAssignment(ProjectTempAssignmentEvent event) {
        InternalNotificationCommand command = InternalNotificationCommand.builder()
                .templateType(AlarmTemplateType.POST_TEMP)
                .targetUserIds(List.of(event.userId()))
                .variables(Map.of("projectName", event.projectName()))
                .targetType(TargetType.POST)
                .targetId(event.projectId())
                .linkUrl("/projects/" + event.projectId() + "/assignments")
                .build();
        notificationPort.notify(command);
    }
}
