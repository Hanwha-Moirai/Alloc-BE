package com.moirai.alloc.admin.command.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminTechStackSaveRequest {

    @NotBlank
    private String techName;
}
