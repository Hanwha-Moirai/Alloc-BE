package com.moirai.alloc.management.api;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.command.dto.RegisterProjectCommandDTO;
import com.moirai.alloc.management.command.service.RegisterProject;
import com.moirai.alloc.management.query.dto.project_detail.ProjectDetailViewDTO;
import com.moirai.alloc.management.query.dto.project_list.ProjectListItemDTO;
import com.moirai.alloc.management.query.dto.registration.ProjectRegistrationViewDTO;
import com.moirai.alloc.management.query.service.GetProjectDetail;
import com.moirai.alloc.management.query.service.GetProjectList;
import com.moirai.alloc.management.query.service.GetProjectRegistrationView;
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

    // 프로젝트 목록 조회(사용자 + PM 가능)
    @GetMapping
    public List<ProjectListItemDTO> getProjects(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return getProjectList.getProjectList(principal.userId());
    }

    //프로젝트 상세 조회(사용자 + PM 가능)
    @GetMapping("/{projectId}")
    public ProjectDetailViewDTO getProjectDetail(
            @PathVariable Long projectId
    ) {
        return getProjectDetail.getProjectDetail(projectId);
    }
    // 프로젝트 등록 - 초기 데이터 조회(드롭다운) (PM만 가능)
    @GetMapping("/registration-view")
    @PreAuthorize("hasRole('PM')")
    public ProjectRegistrationViewDTO getProjectRegistrationView() {
        return getProjectRegistrationView.getView();
    }

    // 프로젝트 등록 -  입력(PM만 가능)
    @PostMapping
    @PreAuthorize("hasRole('PM')")
    public Map<String, Long> registerProject(
            @RequestBody RegisterProjectCommandDTO command
    ) {
        Long projectId = registerProject.registerProject(command);
        return Map.of("projectId", projectId);
    }
}
