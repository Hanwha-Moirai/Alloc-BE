package com.moirai.alloc.home.query.service;

import com.moirai.alloc.home.query.dto.HomeProjectListItemDTO;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly=true)
public class GetHomeProjectList {
    private final SquadAssignmentRepository squadAssignmentRepository;
    public GetHomeProjectList(SquadAssignmentRepository squadAssignmentRepository) {
        this.squadAssignmentRepository = squadAssignmentRepository;
    }
    public List<HomeProjectListItemDTO> getHomeProjectList(Long userId){
//        1) userId를 기준으로 사용자 식별
//        2) 해당 사용자가 참여하고 참여했던(진행/종료) 프로젝트 목록을 조회한다.
//        3) 프로젝트 목록을 조회용 형태로 반환한다.
        List<Project> projects =
                squadAssignmentRepository.findProjectsByUserId(userId);

        return projects.stream()
                .map(project -> new HomeProjectListItemDTO(
                        project.getProjectId(),
                        project.getName(),
                        project.getStartDate(),
                        project.getEndDate(),
                        project.getProjectStatus(),
                        null   // progressRate (타 팀원)
                ))
                .toList();
    }
}
