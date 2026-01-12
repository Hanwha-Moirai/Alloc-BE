package com.moirai.alloc.management.command.dto;

import lombok.Getter;

@Getter
public class JobRequirementRequestDTO {
    private Long jobId;
    private int requiredCount;
}
