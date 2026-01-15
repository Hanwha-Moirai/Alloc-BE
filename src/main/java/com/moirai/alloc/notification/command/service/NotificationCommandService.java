package com.moirai.alloc.notification.command.service;

import com.moirai.alloc.notification.command.domain.entity.*;
import com.moirai.alloc.notification.command.dto.request.InternalNotificationCreateRequest;
import com.moirai.alloc.notification.command.dto.response.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import com.moirai.alloc.notification.command.repository.AlarmSendLogRepository;
import com.moirai.alloc.notification.command.repository.AlarmTemplateRepository;
import com.moirai.alloc.notification.common.event.AlarmCreatedEvent;
import com.moirai.alloc.notification.common.event.AlarmUnreadChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "transactionManager")
public class NotificationCommandService {

    private final AlarmLogRepository alarmLogRepository;
    private final AlarmTemplateRepository alarmTemplateRepository;
    private final AlarmSendLogRepository alarmSendLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 시스템 내부 알림 생성
     * POST /api/internal/notifications
     */
    public InternalNotificationCreateResponse createInternalNotifications(InternalNotificationCreateRequest request) {

        AlarmTemplate template = alarmTemplateRepository
                .findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(request.getTemplateType())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "알림 템플릿을 찾을 수 없습니다. type=" + request.getTemplateType()
                ));

        String mergedTitle = mergeVariables(template.getTemplateTitle(), request.getVariables());
        String mergedBody  = mergeVariables(template.getTemplateContext(), request.getVariables());

        List<AlarmLog> logs = request.getTargetUserIds().stream()
                .map(uid -> AlarmLog.builder()
                        .userId(uid)
                        .templateId(template.getId())
                        .alarmTitle(mergedTitle)
                        .alarmContext(mergedBody)
                        .targetType(request.getTargetType())
                        .targetId(request.getTargetId())
                        .linkUrl(request.getLinkUrl())
                        .build())
                .toList();

        List<AlarmLog> saved = alarmLogRepository.saveAll(logs);

        // 발송 로그(요구 ERD: alarm_send_log.user_id 존재) -> 수신자별 1건 기록
        List<AlarmSendLog> sendLogs = saved.stream()
                .map(alarm -> AlarmSendLog.builder()
                        .templateId(template.getId())
                        .userId(alarm.getUserId())
                        .logStatus(SendLogStatus.SUCCESS)
                        .templateContext(mergedBody)
                        .sentAt(LocalDateTime.now())
                        .build())
                .toList();

        alarmSendLogRepository.saveAll(sendLogs);

        // 커밋 후 SSE 전송은 핸들러에서 처리(AFTER_COMMIT)
        for (AlarmLog alarm : saved) {
            eventPublisher.publishEvent(AlarmCreatedEvent.builder()
                    .userId(alarm.getUserId())
                    .alarmId(alarm.getId())
                    .title(alarm.getAlarmTitle())
                    .content(alarm.getAlarmContext())
                    .targetType(alarm.getTargetType())
                    .targetId(alarm.getTargetId())
                    .linkUrl(alarm.getLinkUrl())
                    .createdAt(alarm.getCreatedAt())
                    .build());
        }

        return InternalNotificationCreateResponse.builder()
                .createdCount(saved.size())
                .alarmIds(saved.stream().map(AlarmLog::getId).toList())
                .build();
    }

    // 사용자 액션

    public void markRead(Long userId, Long notificationId) {
        int updated = alarmLogRepository.markRead(userId, notificationId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");
        }
        eventPublisher.publishEvent(AlarmUnreadChangedEvent.builder().userId(userId).build());
    }

    public void markAllRead(Long userId) {
        alarmLogRepository.markAllRead(userId);
        eventPublisher.publishEvent(AlarmUnreadChangedEvent.builder().userId(userId).build());
    }

    public void deleteNotification(Long userId, Long notificationId) {
        int updated = alarmLogRepository.softDeleteOne(userId, notificationId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");
        }
        eventPublisher.publishEvent(AlarmUnreadChangedEvent.builder().userId(userId).build());
    }

    public void deleteAllRead(Long userId) {
        alarmLogRepository.softDeleteAllRead(userId);
        eventPublisher.publishEvent(AlarmUnreadChangedEvent.builder().userId(userId).build());
    }

    // 헬퍼

    private String mergeVariables(String text, Map<String, String> variables) {
        if (text == null || variables == null || variables.isEmpty()) return text;

        String merged = text;
        for (Map.Entry<String, String> e : variables.entrySet()) {
            String placeholder = "{{" + e.getKey() + "}}";
            merged = merged.replace(placeholder, Objects.toString(e.getValue(), ""));
        }
        return merged;
    }
}
