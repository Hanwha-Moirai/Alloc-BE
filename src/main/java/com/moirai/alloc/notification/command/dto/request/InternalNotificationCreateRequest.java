package com.moirai.alloc.notification.command.dto.request;

import com.moirai.alloc.notification.command.domain.entity.AlarmTemplateType;
import com.moirai.alloc.notification.command.domain.entity.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class InternalNotificationCreateRequest {

    @NotNull(message = "templateType은 필수입니다.")
    private AlarmTemplateType templateType;

    @NotEmpty(message = "targetUserIds는 최소 1명 이상이어야 합니다.")
    private List<@NotNull(message = "targetUserIds 요소는 null일 수 없습니다.") Long> targetUserIds;

    private Map<
            @NotBlank(message = "variables key는 공백일 수 없습니다.") String,
            @NotNull(message = "variables value는 null일 수 없습니다.") String
            > variables;

    @NotNull(message = "targetType은 필수입니다.")
    private TargetType targetType;

    @NotNull(message = "targetId는 필수입니다.")
    private Long targetId;

    @Size(max = 255, message = "linkUrl은 255자 이하여야 합니다.")
    private String linkUrl;
}
