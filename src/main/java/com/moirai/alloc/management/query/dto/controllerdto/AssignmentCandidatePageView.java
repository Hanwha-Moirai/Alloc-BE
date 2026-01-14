package com.moirai.alloc.management.query.dto.controllerdto;

import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.candidate_list.JobAssignmentSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AssignmentCandidatePageView {
    // 추천된 인력 리스트업 페이지를 위한 컨트롤러용 dto
    private List<JobAssignmentSummaryDTO> jobSummaries;
    private List<AssignmentCandidateItemDTO> candidates;
}
