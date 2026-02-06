package com.moirai.alloc.management.controller;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.AssignUsersCommandDTO;
import com.moirai.alloc.management.command.service.SelectAdditionalAssignmentCandidates;
import com.moirai.alloc.management.command.service.SelectAssignmentCandidates;
import com.moirai.alloc.management.query.dto.candidateList.CandidateScoreFilter;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentCandidatePageView;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentManagementPageView;
import com.moirai.alloc.management.query.dto.selectedList.AssignmentStatusDTO;
import com.moirai.alloc.management.query.service.GetAssignedMembers;
import com.moirai.alloc.management.query.service.GetAssignmentCandidates;
import com.moirai.alloc.management.query.service.GetAssignedStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}")
public class ProjectAssignmentController {

    private final GetAssignmentCandidates getAssignmentCandidates;
    private final GetAssignedStatus getAssignmentStatus;
    private final SelectAssignmentCandidates selectAssignmentCandidates;
    private final SelectAdditionalAssignmentCandidates selectAdditionalAssignmentCandidates;
    private final GetAssignedMembers getAssignedMembers;

    //인력 선발 현황 화면 조회
    @GetMapping("/assign")
    public AssignmentCandidatePageView getAssignmentCandidatePage(
            @PathVariable Long projectId,
            @RequestParam(required = false) Integer skill,
            @RequestParam(required = false) Integer experience,
            @RequestParam(required = false) Integer availability
    ) {
        CandidateScoreFilter filter =
                (skill == null && experience == null && availability == null)
                        ? null
                        : new CandidateScoreFilter(
                        skill != null ? skill : 100,
                        experience != null ? experience : 100,
                        availability != null ? availability : 100
                );

        return getAssignmentCandidates
                .getAssignmentCandidates(projectId, filter);
    }


    //    프로젝트 인력 배치 관리 화면
    @GetMapping("/members")
    public AssignmentManagementPageView getAssignmentManagementPage(
            @PathVariable Long projectId
    ) {
        return new AssignmentManagementPageView(
                getAssignmentStatus.getSummary(projectId),
                getAssignmentStatus.getStatus(projectId),
                getAssignedMembers.getMembers(projectId)
        );
    }


    //최초 인력 선발 저장 (PM만 가능)
    @PreAuthorize("hasRole('PM')")
    @PostMapping("/assignments")
    public void assignCandidates(
            @PathVariable Long projectId,
            @RequestBody AssignUsersCommandDTO request
    ) {
        selectAssignmentCandidates
                .selectByUserIds(projectId, request.getUserIds());
    }

    //프로젝트 인력 배치 현황 조회(사용자 / PM 가능)
    @GetMapping("/assignments")
    public AssignmentStatusDTO getAssignmentStatus(
            @PathVariable Long projectId
    ) {
        return getAssignmentStatus.getStatus(projectId);
    }

    // 추가 후보 생성 (PM)
    @PreAuthorize("hasRole('PM')")
    @PostMapping("/assignments/additional")
    public void addMoreCandidates(
            @PathVariable Long projectId
    ) {
        selectAdditionalAssignmentCandidates
                .selectAdditionalCandidates(projectId);
    }


}
