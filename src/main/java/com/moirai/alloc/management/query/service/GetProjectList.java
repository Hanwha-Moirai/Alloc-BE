package com.moirai.alloc.management.query.service;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.projectList.ProjectListItemDTO;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional(readOnly = true)
public class GetProjectList {

    private final SquadAssignmentRepository squadAssignmentRepository;

    public GetProjectList(SquadAssignmentRepository squadAssignmentRepository) {
        this.squadAssignmentRepository = squadAssignmentRepository;
    }

    public Page<ProjectListItemDTO> getProjectList(Long userId, Pageable pageable) {
        Page<Project> projects =
                squadAssignmentRepository.findProjectsByUserId(userId, pageable);

        return projects.map(project -> new ProjectListItemDTO(
                project.getProjectId(),
                project.getName(),
                project.getStartDate(),
                project.getEndDate(),
                project.getProjectStatus(),
                null,   // progressRate
                null,   // documentStatus
                null    // riskLevel
        ));
    }
}
