package com.moirai.alloc.management.query.dto.candidate_list;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobAssignmentSummaryDTO {
    private Long jobId;
    private String jobName;
    private int selectedCount;
    private int requiredCount;
    private JobAssignmentStatus status;
}
