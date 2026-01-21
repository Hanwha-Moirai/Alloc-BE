package com.moirai.alloc.notification.common.contract;

import lombok.Builder;

import java.util.*;

@Builder
public record InternalNotificationCommand(
        AlarmTemplateType templateType,
        List<Long> targetUserIds,
        Map<String, String> variables,
        TargetType targetType,
        Long targetId,
        String linkUrl
) {
    public InternalNotificationCommand {
        Objects.requireNonNull(templateType, "templateType는 필수입니다.");
        Objects.requireNonNull(targetType, "targetType은 필수입니다.");
        Objects.requireNonNull(targetId, "targetId는 필수입니다.");

        if (targetUserIds == null || targetUserIds.isEmpty()) {
            throw new IllegalArgumentException("targetUserIds는 최소 1명 이상이어야 합니다.");
        }

        if (targetUserIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("targetUserIds 요소는 null일 수 없습니다.");
        }


        // 불변/방어적 복사(외부에서 리스트/맵을 수정해도 내부 불변 유지)
        targetUserIds = List.copyOf(targetUserIds);
        variables = (variables == null) ? Map.of() : Map.copyOf(variables);
    }
}
