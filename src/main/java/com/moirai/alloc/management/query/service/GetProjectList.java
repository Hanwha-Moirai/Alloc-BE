package com.moirai.alloc.management.query.service;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.projectList.ProjectListItemDTO;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional(readOnly=true)
public class GetProjectList {
    private final SquadAssignmentRepository squadAssignmentRepository;
    public GetProjectList(SquadAssignmentRepository squadAssignmentRepository) {
        this.squadAssignmentRepository = squadAssignmentRepository;
    }
    public Page<ProjectListItemDTO> getProjectList(Long userId, Pageable pageable) {
//        1) userId를 기준으로 사용자 식별
//        2) 해당 사용자가 참여하고 참여했던(진행/종료) 프로젝트 목록을 조회한다.
//        3) 프로젝트 목록을 조회용 형태로 반환한다.
        Page<Project> projectPage =
                squadAssignmentRepository.findProjectsByUserId(userId, pageable);

        return projectPage.map(project -> new ProjectListItemDTO(
                        project.getProjectId(),
                        project.getName(),
                        project.getStartDate(),
                        project.getEndDate(),
                        project.getProjectStatus(),
                        null,   // progressRate (타 팀원)
                        null,   // documentStatus (타 팀원)
                        null    // riskLevel (타 팀원)
                ));
    }
}
