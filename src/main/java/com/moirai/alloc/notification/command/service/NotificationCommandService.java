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
     * [내부 알림 생성] 템플릿 기반으로 알림 N건 생성 후 SSE 전송 이벤트 발행
     * - alarm_template: templateType으로 최신(미삭제) 템플릿 1건 조회
     * - alarm_log: 수신자(targetUserIds) 수 만큼 생성(soft delete/읽음 상태 기본값 false)
     * - alarm_send_log: 수신자별 발송 기록 1건 생성(감사/장애 분석용 스냅샷 보관)
     * - 이벤트: AlarmCreatedEvent(수신자별) 발행 → AFTER_COMMIT 핸들러가 SSE push + 미읽음 카운트 갱신
     */
    public InternalNotificationCreateResponse createInternalNotifications(InternalNotificationCreateRequest request) {

        AlarmTemplate template = alarmTemplateRepository
                .findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(request.getTemplateType())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "알림 템플릿을 찾을 수 없습니다. type=" + request.getTemplateType()
                ));

        // 템플릿 문자열에 variables를 치환(예: {{taskName}})
        String mergedTitle = mergeVariables(template.getTemplateTitle(), request.getVariables());
        String mergedBody  = mergeVariables(template.getTemplateContext(), request.getVariables());

        // alarm_log: 수신자별 1건 생성
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

        // alarm_send_log: 수신자별 1건 기록(요구 ERD 반영: user_id 존재)
        List<AlarmSendLog> sendLogs = saved.stream()
                .map(alarm -> AlarmSendLog.builder()
                        .templateId(template.getId())
                        .userId(alarm.getUserId())
                        .logStatus(SendLogStatus.SUCCESS)
                        .templateContext(mergedBody) // 당시 바디 스냅샷
                        .sentAt(LocalDateTime.now())
                        .build())
                .toList();

        alarmSendLogRepository.saveAll(sendLogs);

        // 트랜잭션 커밋 이후 SSE 전송(핸들러에서 AFTER_COMMIT로 처리)
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

    /**
     * [사용자 액션] 단건 읽음 처리
     * - update 조건: (alarmId, userId, deleted=false)
     * - 결과: 0건이면 "본인 알림이 아니거나 존재하지 않음" → 404
     * - SSE: 미읽음 카운트 변경 이벤트 발행
     */
    public void markRead(Long userId, Long notificationId) {
        int updated = alarmLogRepository.markRead(userId, notificationId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");
        }
        eventPublisher.publishEvent(AlarmUnreadChangedEvent.builder().userId(userId).build());
    }

    /**
     * [사용자 액션] 전체 읽음 처리
     * - update 조건: (userId, deleted=false, read=false)
     * - SSE: 미읽음 카운트 변경 이벤트 발행
     */
    public void markAllRead(Long userId) {
        alarmLogRepository.markAllRead(userId);
        eventPublisher.publishEvent(AlarmUnreadChangedEvent.builder().userId(userId).build());
    }

    /**
     * [사용자 액션] 단건 삭제(Soft Delete)
     * - update 조건: (alarmId, userId, deleted=false)
     * - 0건이면 404
     * - SSE: 미읽음 카운트 변경 이벤트 발행(삭제로 미읽음이 감소할 수 있음)
     */
    public void deleteNotification(Long userId, Long notificationId) {
        int updated = alarmLogRepository.softDeleteOne(userId, notificationId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");
        }
        eventPublisher.publishEvent(AlarmUnreadChangedEvent.builder().userId(userId).build());
    }

    /**
     * [사용자 액션] 읽음 처리된 알림 전체 삭제(Soft Delete)
     * - update 조건: (userId, deleted=false, read=true)
     * - SSE: 미읽음 카운트 변경 이벤트 발행
     */
    public void deleteAllRead(Long userId) {
        alarmLogRepository.softDeleteAllRead(userId);
        eventPublisher.publishEvent(AlarmUnreadChangedEvent.builder().userId(userId).build());
    }

    /**
     * 템플릿 변수 치환 헬퍼
     * - "{{key}}" 형태의 placeholder를 variables 값으로 replace
     * - variables가 null/empty면 원문 반환
     */
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
