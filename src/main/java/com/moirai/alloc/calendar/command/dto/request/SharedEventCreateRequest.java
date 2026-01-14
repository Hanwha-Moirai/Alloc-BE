package com.moirai.alloc.calendar.command.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class SharedEventCreateRequest {
    @NotBlank
    private String eventName;

    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    private String place;

    private String description;

    @NotEmpty(message = "공유 일정은 참여자 목록(memberUserIds)이 필수입니다.")
    private List<@NotNull Long> memberUserIds;
}
