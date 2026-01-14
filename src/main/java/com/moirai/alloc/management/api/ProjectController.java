package com.moirai.alloc.management.api;

import com.moirai.alloc.management.command.dto.RegisterProjectCommandDTO;
import com.moirai.alloc.management.command.service.RegisterProject;
import com.moirai.alloc.management.query.dto.project_detail.ProjectDetailViewDTO;
import com.moirai.alloc.management.query.dto.project_list.ProjectListItemDTO;
import com.moirai.alloc.management.query.service.GetProjectDetail;
import com.moirai.alloc.management.query.service.GetProjectList;
import lombok.RequiredArgsConstructor;
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

    // 프로젝트 목록 조회
    @GetMapping
    public List<ProjectListItemDTO> getProjects(
            @RequestParam Long userId
    ) {
        return getProjectList.getProjectList(userId);
    }

    //프로젝트 상세 조회
    @GetMapping("/{projectId}")
    public ProjectDetailViewDTO getProjectDetail(
            @PathVariable Long projectId
    ) {
        return getProjectDetail.getProjectDetail(projectId);
    }

    // 프로젝트 등록
    @PostMapping
    public Map<String, Long> registerProject(
            @RequestBody RegisterProjectCommandDTO command
    ) {
        Long projectId = registerProject.registerProject(command);
        return Map.of("projectId", projectId);
    }
}
