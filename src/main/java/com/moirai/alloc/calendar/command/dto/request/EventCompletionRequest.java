package com.moirai.alloc.calendar.command.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class EventCompletionRequest {
    @NotNull
    private Boolean completed;
}
