package com.moirai.alloc.management.query.view;

import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.candidate_list.JobAssignmentSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AssignmentCandidatesView {
    private List<JobAssignmentSummaryDTO> jobSummaries;
    private List<AssignmentCandidateItemDTO> candidates;
}
