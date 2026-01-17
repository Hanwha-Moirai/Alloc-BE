package com.moirai.alloc.management.command.service;
import com.moirai.alloc.management.command.dto.RegisterProjectCommandDTO;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.domain.vo.TechRequirement;
import com.moirai.alloc.project.command.domain.Project;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class RegisterProject {
    private final ProjectRepository projectRepository;
    private final AssignProjectManager assignProjectManager;
    public RegisterProject(ProjectRepository projectRepository, AssignProjectManager assignProjectManager) {
        this.projectRepository = projectRepository;
        this.assignProjectManager = assignProjectManager;
    }
    public Long registerProject(RegisterProjectCommandDTO command, Long pmUserId ) {
// 1) JobStandard 및 TechStandard를 기반으로 프로젝트 등록 화면의 초기 데이터를 조회한다.
// 2) 프로젝트에 필요한 직군 요구사항, 기술 요구사항, 기간, 예산, 설명 등의 정보를 입력한다.
// 3) 프로젝트를 저장하여 식별자(projectId)를 생성한다.
// 4) 생성된 프로젝트 식별자를 기준으로 Project Manager를 자동으로 배정한다.
        Project project = Project.builder()
                .name(command.getName())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .partners(command.getPartners())
                .predictedCost(command.getPredictedCost())
                .projectType(command.getProjectType())
                .description(command.getDescription())
                .build();

        // 프로젝트 직군 요구사항 설정; 드롭다운에서 선택
        List<JobRequirement> jobReqs =
                command.getJobRequirements().stream()
                        .map(req -> new JobRequirement(
                                req.getJobId(),
                                req.getRequiredCount()
                        ))
                        .toList();
        project.changeJobRequirements(jobReqs);

        // 프로젝트 기술 요구사항 설정; 드롭다운에서 선택
        List<TechRequirement> techReqs =
                command.getTechRequirements().stream()
                        .map(req -> new TechRequirement(
                                req.getTechId(),
                                req.getTechLevel()
                        ))
                        .toList();
        project.changeTechRequirements(techReqs);

        // 프로젝트 저장, projectId 생성
        Long projectId =
                projectRepository.save(project).getProjectId();

        // 도메인 정책 실행; 프로젝트 생성시 생성한사용자(pm)은 자동 참여한다.
        assignProjectManager.assignPm(projectId, pmUserId);

        return projectId;

    }
}
