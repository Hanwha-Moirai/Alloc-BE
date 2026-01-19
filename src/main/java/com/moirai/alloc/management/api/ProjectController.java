package com.moirai.alloc.management.api;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.command.dto.EditProjectDTO;
import com.moirai.alloc.management.command.dto.RegisterProjectCommandDTO;
import com.moirai.alloc.management.command.service.EditProject;
import com.moirai.alloc.management.command.service.RegisterProject;
import com.moirai.alloc.management.query.dto.projectDetail.ProjectDetailViewDTO;
import com.moirai.alloc.management.query.dto.projectList.ProjectListItemDTO;
import com.moirai.alloc.management.query.dto.registration.ProjectRegistrationViewDTO;
import com.moirai.alloc.management.query.service.GetProjectDetail;
import com.moirai.alloc.management.query.service.GetProjectList;
import com.moirai.alloc.management.query.service.GetProjectRegistrationView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final GetProjectList getProjectList;
    private final GetProjectDetail getProjectDetail;
    private final RegisterProject registerProject;
    private final GetProjectRegistrationView getProjectRegistrationView;
    private final EditProject editProject;
    // 프로젝트 목록 조회 (USER + PM)
    @GetMapping
    public List<ProjectListItemDTO> getProjects(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return getProjectList.getProjectList(principal.userId());
    }

    // 프로젝트 상세 조회 (USER + PM)
    @GetMapping("/{projectId}")
    public ProjectDetailViewDTO getProjectDetail(
            @PathVariable Long projectId
    ) {
        return getProjectDetail.getProjectDetail(projectId);
    }

    // 프로젝트 등록 초기 데이터 조회 (PM만)
    @GetMapping("/registration-view")
    @PreAuthorize("hasRole('PM')")
    public ProjectRegistrationViewDTO getProjectRegistrationView() {
        return getProjectRegistrationView.getView();
    }

    // 프로젝트 등록 (PM만)
    @PostMapping
    @PreAuthorize("hasRole('PM')")
    public Map<String, Long> registerProject(
            @Valid @RequestBody RegisterProjectCommandDTO command,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long projectId =
                registerProject.registerProject(command, principal.userId());

        return Map.of("projectId", projectId);
    }

    //프로젝트 기본 정보 수정 (PM 전용)
    @PreAuthorize("hasRole('PM')")
    @PutMapping("/{projectId}")
    public void updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody EditProjectDTO command
    ) {
        if (!projectId.equals(command.getProjectId())) {
            throw new IllegalArgumentException("Project ID mismatch");
        }
        editProject.update(command);
    }
}
