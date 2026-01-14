package com.moirai.alloc.management.api;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.service.SelectAdditionalAssignmentCandidates;
import com.moirai.alloc.management.command.service.SelectAssignmentCandidates;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidatesView;
import com.moirai.alloc.management.query.dto.controller_dto.AssignmentCandidatePageView;
import com.moirai.alloc.management.query.dto.controller_dto.AssignmentManagementPageView;
import com.moirai.alloc.management.query.dto.select_list.AssignmentStatusDTO;
import com.moirai.alloc.management.query.service.GetAssignmentCandidates;
import com.moirai.alloc.management.query.service.GetAssignmentMembers;
import com.moirai.alloc.management.query.service.GetAssignmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}")
public class ProjectAssignmentController {

    private final GetAssignmentCandidates getAssignmentCandidates;
    private final GetAssignmentMembers getAssignmentMembers;
    private final GetAssignmentStatus getAssignmentStatus;
    private final SelectAssignmentCandidates selectAssignmentCandidates;
    private final SelectAdditionalAssignmentCandidates selectAdditionalAssignmentCandidates;

    // 인력 추천 및 선발 현황 화면 조회(선발현황 + 후보 리스트)
    @GetMapping("/assign")
    public AssignmentCandidatePageView getAssignmentCandidatePage(
            @PathVariable Long projectId
    ) {
        AssignmentCandidatesView view =
                getAssignmentCandidates.getAssignmentCandidates(projectId);

        return new AssignmentCandidatePageView(
                view.getJobSummaries(),
                view.getCandidates()
        );
    }

    // 프로젝트 상세보기; 인력 선발 현황 + 면담/승인 등 인력 배치 현황
    @GetMapping("/members")
    public AssignmentManagementPageView getAssignmentManagementPage(
            @PathVariable Long projectId
    ) {
        return new AssignmentManagementPageView(
                getAssignmentStatus.getSummary(projectId),
                getAssignmentStatus.getStatus(projectId),
                getAssignmentMembers.getMembers(projectId)
        );
    }

    //선발한 최초 인력 선발 저장
    @PostMapping("/assignments")
    public void assignCandidates(
            @PathVariable Long projectId,
            @RequestBody AssignCandidateDTO command
    ) {
        if (!projectId.equals(command.getProjectId())) {
            throw new IllegalArgumentException("Project ID mismatch");
        }
        selectAssignmentCandidates.selectAssignmentCandidates(command);
    }

    // 선발한 인력 배치 현황 조회
    @GetMapping("/assignments")
    public AssignmentStatusDTO getAssignmentStatus(
            @PathVariable Long projectId
    ) {
        return getAssignmentStatus.getStatus(projectId);
    }

    //선발 이후, 부족 인원 발생 → 추가 후보 생성
    @PostMapping("/assignments/additional")
    public void addMoreCandidates(
            @PathVariable Long projectId
    ) {
        selectAdditionalAssignmentCandidates
                .selectAdditionalCandidates(projectId);
    }
}