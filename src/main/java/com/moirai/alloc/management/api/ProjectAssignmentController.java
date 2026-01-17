package com.moirai.alloc.management.api;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.service.SelectAdditionalAssignmentCandidates;
import com.moirai.alloc.management.command.service.SelectAssignmentCandidates;
import com.moirai.alloc.management.query.dto.selectedWorker.AssignedMemberDTO;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentCandidatePageView;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentManagementPageView;
import com.moirai.alloc.management.query.dto.selectedList.AssignmentStatusDTO;
import com.moirai.alloc.management.query.service.GetAssignedMembers;
import com.moirai.alloc.management.query.service.GetAssignmentCandidates;
import com.moirai.alloc.management.query.service.GetAssignmentMembers;
import com.moirai.alloc.management.query.service.GetAssignmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}")
public class ProjectAssignmentController {

    private final GetAssignmentCandidates getAssignmentCandidates;
    private final GetAssignmentMembers getAssignmentMembers; //배치 관리 화면용 유스케이스
    private final GetAssignmentStatus getAssignmentStatus;
    private final SelectAssignmentCandidates selectAssignmentCandidates;
    private final SelectAdditionalAssignmentCandidates selectAdditionalAssignmentCandidates;
    private final GetAssignedMembers getAssignedMembers; //최종 확정 인원 조회 유스케이스
    //인력 추천 및 선발 현황 화면 조회
    //이미 선택된 인원 + 추천 후보를 하나의 리스트로 반환
    //사용자 / PM 모두 접근 가능

    @GetMapping("/assign")
    public AssignmentCandidatePageView getAssignmentCandidatePage(
            @PathVariable Long projectId
    ) {
        return getAssignmentCandidates.getAssignmentCandidates(projectId);
    }

//    프로젝트 인력 배치 관리 화면
//    선발된 인원 목록
//    응답 상태 요약
//    직군별 부족 인원 현황
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


     //최초 인력 선발 저장 (PM만 가능)

    @PreAuthorize("hasRole('PM')")
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

    //프로젝트 인력 배치 현황 조회(사용자 / PM 가능)
    @GetMapping("/assignments")
    public AssignmentStatusDTO getAssignmentStatus(
            @PathVariable Long projectId
    ) {
        return getAssignmentStatus.getStatus(projectId);
    }

    //추가 후보 생성 (pm만)
    @PreAuthorize("hasRole('PM')")
    @PostMapping("/assignments/additional")
    public void addMoreCandidates(
            @PathVariable Long projectId
    ) {
        selectAdditionalAssignmentCandidates
                .selectAdditionalCandidates(projectId);
    }
    // 최종 선정된 팀원 리스트
    @GetMapping("/assignments/assigned")
    @PreAuthorize("hasRole('PM') or hasRole('USER')")
    public List<AssignedMemberDTO> getAssignedMembers(
            @PathVariable Long projectId
    ) {
        return getAssignedMembers.getAssignedMembers(projectId);
    }

}
