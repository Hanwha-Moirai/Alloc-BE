package com.moirai.alloc.management.query.service;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.projectList.ProjectListItemDTO;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetProjectList {

    private final SquadAssignmentRepository squadAssignmentRepository;

    public GetProjectList(SquadAssignmentRepository squadAssignmentRepository) {
        this.squadAssignmentRepository = squadAssignmentRepository;
    }

    public List<ProjectListItemDTO> getProjectList(Long userId) {

        List<Project> projects =
                squadAssignmentRepository.findProjectsByUserId(userId);

        return projects.stream()
                .map(project -> new ProjectListItemDTO(
                        project.getProjectId(),
                        project.getName(),
                        project.getStartDate(),
                        project.getEndDate(),
                        project.getProjectStatus(),
                        null,   // progressRate
                        null,   // documentStatus
                        null    // riskLevel
                ))
                .toList();
    }
}
