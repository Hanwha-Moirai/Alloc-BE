package com.moirai.alloc.management.query.dto.controller_dto;

import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.select_list.AssignmentStatusDTO;
import com.moirai.alloc.management.query.dto.select_list.AssignmentSummaryCardDTO;
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
