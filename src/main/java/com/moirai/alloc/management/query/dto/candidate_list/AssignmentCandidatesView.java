package com.moirai.alloc.management.query.dto.candidate_list;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AssignmentCandidatesView {
    private List<JobAssignmentSummaryDTO> jobSummaries;
    private List<AssignmentCandidateItemDTO> candidates;
}
