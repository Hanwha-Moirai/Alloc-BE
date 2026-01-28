package com.moirai.alloc.notification.command.event;

import com.moirai.alloc.gantt.command.event.MilestoneCreatedEvent;
import com.moirai.alloc.gantt.command.event.TaskAssigneeAssignedEvent;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
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
public class NotificationDomainEventHandler {

    private final NotificationPort notificationPort;
    private final SquadAssignmentRepository squadAssignmentRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskAssigneeAssigned(TaskAssigneeAssignedEvent event) {
        if (event.assigneeId() == null) {
            return;
        }
        InternalNotificationCommand command = InternalNotificationCommand.builder()
                .templateType(AlarmTemplateType.TASK_ASSIGN)
                .targetUserIds(List.of(event.assigneeId()))
                .variables(Map.of("taskName", event.taskName()))
                .targetType(TargetType.TASK)
                .targetId(event.taskId())
                .linkUrl("/projects/" + event.projectId() + "/tasks/" + event.taskId())
                .build();
        notificationPort.notify(command);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMilestoneCreated(MilestoneCreatedEvent event) {
        List<Long> targetUserIds = squadAssignmentRepository.findAssignedByProjectId(event.projectId()).stream()
                .map(SquadAssignment::getUserId)
                .distinct()
                .toList();
        if (targetUserIds.isEmpty()) {
            return;
        }
        InternalNotificationCommand command = InternalNotificationCommand.builder()
                .templateType(AlarmTemplateType.MILESTONE)
                .targetUserIds(targetUserIds)
                .variables(Map.of("milestoneName", event.milestoneName()))
                .targetType(TargetType.MILESTONE)
                .targetId(event.milestoneId())
                .linkUrl("/projects/" + event.projectId() + "/milestones/" + event.milestoneId())
                .build();
        notificationPort.notify(command);
    }

}
