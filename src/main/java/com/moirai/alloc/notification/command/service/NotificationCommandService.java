package com.moirai.alloc.notification.command.service;

import com.moirai.alloc.notification.command.domain.entity.*;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import com.moirai.alloc.notification.command.repository.AlarmSendLogRepository;
import com.moirai.alloc.notification.command.repository.AlarmTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class NotificationCommandService {

    private final AlarmLogRepository alarmLogRepository;
    private final AlarmTemplateRepository alarmTemplateRepository;
    private final AlarmSendLogRepository alarmSendLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * [내부 알림 생성] 템플릿 기반으로 알림 N건 생성
     * - alarm_template: templateType으로 최신(미삭제) 템플릿 1건 조회
     * - alarm_log: 수신자(targetUserIds) 수 만큼 생성(soft delete/읽음 상태 기본값 false)
     * - alarm_send_log: 수신자별 발송 기록 1건 생성(감사/장애 분석용 스냅샷 보관)
     * - 이벤트: AlarmCreatedEvent(수신자별) 발행 → AFTER_COMMIT 핸들러가 SSE push + 미읽음 카운트 갱신
     */
    public InternalNotificationCreateResponse createInternalNotifications(InternalNotificationCommand cmd) {

        log.info("Creating notifications templateType={} targetUserIds={} targetType={} targetId={} linkUrl={}",
                cmd.templateType(), cmd.targetUserIds(), cmd.targetType(), cmd.targetId(), cmd.linkUrl());

        AlarmTemplate template = alarmTemplateRepository
                .findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(cmd.templateType())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "알림 템플릿을 찾을 수 없습니다. type=" + cmd.templateType()
                ));

        log.info("Resolved alarm template id={} type={} title={}",
                template.getId(), template.getAlarmTemplateType(), template.getTemplateTitle());

        String mergedTitle = mergeVariables(template.getTemplateTitle(), cmd.variables());
        String mergedBody  = mergeVariables(template.getTemplateContext(), cmd.variables());

        List<AlarmLog> logs = cmd.targetUserIds().stream()
                .map(uid -> AlarmLog.builder()
                        .userId(uid)
                        .templateId(template.getId())
                        .alarmTitle(mergedTitle)
                        .alarmContext(mergedBody)
                        .targetType(cmd.targetType())
                        .targetId(cmd.targetId())
                        .linkUrl(cmd.linkUrl())
                        .build())
                .toList();

        List<AlarmLog> saved = alarmLogRepository.saveAll(logs);
        log.info("Saved alarm_log count={} ids={}",
                saved.size(), saved.stream().map(AlarmLog::getId).toList());

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
        log.info("Saved alarm_send_log count={}", sendLogs.size());

        return InternalNotificationCreateResponse.builder()
                .createdCount(saved.size())
                .alarmIds(saved.stream().map(AlarmLog::getId).toList())
                .build();
    }

    /**
     * [사용자 액션] 단건 읽음 처리
     * - update 조건: (alarmId, userId, deleted=false)
     * - 결과: 0건이면 "본인 알림이 아니거나 존재하지 않음" → 404
     */
    public void markRead(Long userId, Long notificationId) {
        int updated = alarmLogRepository.markRead(userId, notificationId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");
        }
    }

    /**
     * [사용자 액션] 전체 읽음 처리
     * - update 조건: (userId, deleted=false, read=false)
     */
    public void markAllRead(Long userId) {
        alarmLogRepository.markAllRead(userId);
    }

    /**
     * [사용자 액션] 단건 삭제(Soft Delete)
     * - update 조건: (alarmId, userId, deleted=false)
     * - 0건이면 404
     */
    public void deleteNotification(Long userId, Long notificationId) {
        int updated = alarmLogRepository.softDeleteOne(userId, notificationId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");
        }
    }

    /**
     * [사용자 액션] 읽음 처리된 알림 전체 삭제(Soft Delete)
     * - update 조건: (userId, deleted=false, read=true)
     */
    public void deleteAllRead(Long userId) {
        alarmLogRepository.softDeleteAllRead(userId);
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
