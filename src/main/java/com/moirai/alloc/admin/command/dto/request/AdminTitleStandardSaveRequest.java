package com.moirai.alloc.admin.command.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminTitleStandardSaveRequest {

    @NotBlank
    private String titleName;

    @NotNull
    @PositiveOrZero
    private Integer monthlyCost;
}