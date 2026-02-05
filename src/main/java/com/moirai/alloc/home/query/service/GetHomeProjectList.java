package com.moirai.alloc.home.query.service;

import com.moirai.alloc.gantt.query.application.GanttQueryService;
import com.moirai.alloc.home.query.dto.HomeProjectListItemDTO;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetHomeProjectList {

    private final SquadAssignmentRepository squadAssignmentRepository;
    private final GanttQueryService ganttQueryService;

    public GetHomeProjectList(
            SquadAssignmentRepository squadAssignmentRepository,
            GanttQueryService ganttQueryService) {
        this.squadAssignmentRepository = squadAssignmentRepository;
        this.ganttQueryService = ganttQueryService;
    }

    public List<HomeProjectListItemDTO> getHomeProjectList(Long userId) {

        List<Project> projects =
                squadAssignmentRepository.findProjectsByUserId(userId);

        return projects.stream()
                .map(project -> {

                    Double rate =
                            ganttQueryService.findMilestoneCompletionRate(
                                    project.getProjectId()
                            );

                    return new HomeProjectListItemDTO(
                            project.getProjectId(),
                            project.getName(),
                            project.getStartDate(),
                            project.getEndDate(),
                            project.getProjectStatus(),
                            rate == null ? 0 : rate.intValue()
                    );
                })
                .toList();
    }
}
