package com.moirai.alloc.management.event;

import com.moirai.alloc.management.command.event.ProjectFinalAssignmentEvent;
import com.moirai.alloc.management.command.event.ProjectTempAssignmentEvent;
import com.moirai.alloc.notification.common.contract.AlarmTemplateType;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.TargetType;
import com.moirai.alloc.notification.common.port.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ManagementNotificationEventHandler {

    private final NotificationPort notificationPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onProjectTempAssignment(ProjectTempAssignmentEvent event) {
        log.info("Handling ProjectTempAssignmentEvent projectId={} userId={} projectName={}",
                event.projectId(), event.userId(), event.projectName());
        InternalNotificationCommand command = InternalNotificationCommand.builder()
                .templateType(AlarmTemplateType.POST_TEMP)
                .targetUserIds(List.of(event.userId()))
                .variables(Map.of("projectName", event.projectName()))
                .targetType(TargetType.POST)
                .targetId(event.projectId())
                .linkUrl("/projects/" + event.projectId() + "/members")
                .build();
        notificationPort.notify(command);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onProjectFinalAssignment(ProjectFinalAssignmentEvent event) {
        log.info("Handling ProjectFinalAssignmentEvent projectId={} userId={} projectName={}",
                event.projectId(), event.userId(), event.projectName());
        InternalNotificationCommand command = InternalNotificationCommand.builder()
                .templateType(AlarmTemplateType.POST_FINAL)
                .targetUserIds(List.of(event.userId()))
                .variables(Map.of("projectName", event.projectName()))
                .targetType(TargetType.POST)
                .targetId(event.projectId())
                .linkUrl("/projects/" + event.projectId() + "/members")
                .build();
        notificationPort.notify(command);
    }
}
