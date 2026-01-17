package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.command.dto.EditProjectDTO;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EditProject {

    private final ProjectRepository projectRepository;

    public void update(EditProjectDTO command) {

        Project project = projectRepository.findById(command.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        project.updateBasicInfo(
                command.getProjectName(),
                command.getStartDate(),
                command.getEndDate(),
                command.getPartners(),
                command.getDescription(),
                command.getPredictedCost()
        );
    }
}
