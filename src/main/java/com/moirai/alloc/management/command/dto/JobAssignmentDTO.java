package com.moirai.alloc.management.command.dto;

import lombok.Getter;

import java.util.List;
@Getter
public class JobAssignmentDTO {
    private Long jobId;
    private List<ScoredCandidateDTO> candidates;

    public JobAssignmentDTO(
            Long jobId,
            List<ScoredCandidateDTO> candidates
    ) {
        this.jobId = jobId;
        this.candidates = candidates;
    }
}
// 어떤 직군, 직군에 선택된 사람들