package com.moirai.alloc.management.query.dto.controllerDto;

import com.moirai.alloc.management.query.dto.candidateList.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.candidateList.JobAssignmentSummaryDTO;
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
