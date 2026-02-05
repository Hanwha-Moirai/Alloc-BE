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
// 특정 직군에 대하여 추천된 후보 리스트
