package com.moirai.alloc.management.query;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.query.dto.ProjectDetailViewDTO;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class GetProjectDetail {
    private ProjectRepository projectRepository;
    public GetProjectDetail(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectDetailViewDTO getProjectDetail(Long projectId) {
//        1) projectId로 프로젝트를 식별한다
//        2) 프로젝트 상세 정보를 조회한다.
//        3) 조회 결과를 반환한다
        Project project = projectRepository.findById(projectId).orElseThrow(()->new IllegalArgumentException("Project not found!"));
        return new ProjectDetailViewDTO(
                project.getProjectId(),
                project.getName(),
                project.getProjectStatus(),
                project.getProjectType(),
                project.getStartDate(),
                project.getEndDate(),
                project.getPartners(),
                project.getDescription(),
                project.getPredictedCost()
        );
    }

}
