package com.moirai.alloc.management.command.dto;

import lombok.Getter;

import java.util.List;
@Getter
public class JobAssignmentDTO {
    private Long jobId;
    private List<Long> userIds;
}
// 어떤 직군, 직군에 선택된 사람들