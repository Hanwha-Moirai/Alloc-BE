package com.moirai.alloc.management.api;


import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.service.SelectAdditionalAssignmentCandidates;
import com.moirai.alloc.management.command.service.SelectAssignmentCandidates;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidatesView;
import com.moirai.alloc.management.query.dto.select_list.AssignmentStatusDTO;
import com.moirai.alloc.management.query.service.GetAssignmentCandidates;
import com.moirai.alloc.management.query.service.GetAssignmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}")
public class ProjectAssignmentController {

    private final GetAssignmentCandidates getAssignmentCandidates;
    private final GetAssignmentStatus getAssignmentStatus;
    private final SelectAssignmentCandidates selectAssignmentCandidates;
    private final SelectAdditionalAssignmentCandidates selectAdditionalAssignmentCandidates;

    //인력 후보 리스트 조회
    @GetMapping("/assign/candidates")
    public AssignmentCandidatesView getCandidates(
            @PathVariable Long projectId
    ) {
        return getAssignmentCandidates.getAssignmentCandidates(projectId);
    }

    //최초 인력 선발 저장
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

    // 인력 배치 현황 조회
    @GetMapping("/assignments")
    public AssignmentStatusDTO getAssignmentStatus(
            @PathVariable Long projectId
    ) {
        return getAssignmentStatus.getStatus(projectId);
    }

    //부족 인원 발생 → 추가 후보 생성
    @PostMapping("/assignments/additional")
    public void addMoreCandidates(
            @PathVariable Long projectId
    ) {
        selectAdditionalAssignmentCandidates
                .selectAdditionalCandidates(projectId);
    }
}