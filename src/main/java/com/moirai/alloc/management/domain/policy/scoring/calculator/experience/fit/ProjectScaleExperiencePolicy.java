package com.moirai.alloc.management.domain.policy.scoring.calculator.experience.fit;


import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.profile.common.domain.Employee;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProjectScaleExperiencePolicy {

    private final SquadAssignmentRepository assignmentRepository;

    public int calculate(Project project, Employee employee) {
        //이 사람이 비슷한 규모의 프로젝트를 감당해본 적이 있는가?
        //과거 ASSIGNED 프로젝트 중  현재 프로젝트 대비 비용 비율 maxRatio 계산
        Integer currentCost = project.getPredictedCost();

        // 현재 프로젝트 비용 정보 없으면 판단 불가
        if (currentCost == null || currentCost <= 0) {
            return 0;
        }

        List<Project> assignedProjects =
                assignmentRepository.findAssignedProjects(employee.getUserId());

        // 과거 프로젝트 중 가장 큰 비용 비율 계산
        double maxRatio = assignedProjects.stream()
                .map(Project::getPredictedCost)
                .filter(Objects::nonNull)
                .filter(cost -> cost > 0)
                .mapToDouble(cost -> (double) cost / currentCost)
                .max()
                .orElse(0.0);

        // 비율에 따른 점수 산정
        if (maxRatio >= 0.8) {
            return 30;
        }
        if (maxRatio >= 0.5) {
            return 20;
        }
        if (maxRatio >= 0.3) {
            return 10;
        }
        return 0;
    }
}
