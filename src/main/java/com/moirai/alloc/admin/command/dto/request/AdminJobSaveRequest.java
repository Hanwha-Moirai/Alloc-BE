package com.moirai.alloc.admin.command.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminJobSaveRequest {

    @NotBlank
    private String jobName;
}
