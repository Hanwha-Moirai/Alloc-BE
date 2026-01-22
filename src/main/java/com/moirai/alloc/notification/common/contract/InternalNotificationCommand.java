package com.moirai.alloc.notification.common.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.*;

@Builder
public record InternalNotificationCommand(
        @NotNull(message = "templateType는 필수입니다.")
        AlarmTemplateType templateType,

        @NotEmpty(message = "targetUserIds는 최소 1명 이상이어야 합니다.")
        List<@NotNull(message = "targetUserIds 요소는 null일 수 없습니다.") Long> targetUserIds,

        Map<
                @NotBlank(message = "variables key는 공백일 수 없습니다.") String,
                @NotNull(message = "variables value는 null일 수 없습니다.") String
                > variables,

        @NotNull(message = "targetType은 필수입니다.")
        TargetType targetType,

        @NotNull(message = "targetId는 필수입니다.")
        Long targetId,

        @Size(max = 255, message = "linkUrl은 255자 이하여야 합니다.")
        String linkUrl
) {
    public InternalNotificationCommand {
        // 역직렬화 안전 + 불변 컬렉션 보장
        targetUserIds = (targetUserIds == null) ? null : List.copyOf(targetUserIds);
        variables = (variables == null) ? Map.of() : Map.copyOf(variables);
    }
}

