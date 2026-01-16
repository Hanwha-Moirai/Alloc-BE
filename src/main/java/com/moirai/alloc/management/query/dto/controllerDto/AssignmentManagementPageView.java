package com.moirai.alloc.management.query.dto.controllerDto;

import com.moirai.alloc.management.query.dto.candidateList.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.selectedList.AssignmentStatusDTO;
import com.moirai.alloc.management.query.dto.selectedList.AssignmentSummaryCardDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AssignmentManagementPageView {
    // 선발 현황 컨트롤러용 dto
    private AssignmentSummaryCardDTO summary;
    private AssignmentStatusDTO status;
    private List<AssignmentCandidateItemDTO> members;
}
