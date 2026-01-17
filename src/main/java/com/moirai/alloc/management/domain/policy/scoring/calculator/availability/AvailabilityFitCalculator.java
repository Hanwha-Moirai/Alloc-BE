package com.moirai.alloc.management.domain.policy.scoring.calculator.availability;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AvailabilityFitCalculator {

    private final SquadAssignmentRepository assignmentRepository;

    public AvailabilityFitCalculator(SquadAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    public int calculate(Project project, Employee employee) {

        List<Project> assignedProjects =
                assignmentRepository.findAssignedProjects(employee.getUserId());

        long overlappingCount = assignedProjects.stream()
                .filter(p ->
                        !p.getEndDate().isBefore(project.getStartDate()) &&
                                !p.getStartDate().isAfter(project.getEndDate())
                )
                .count();

        // STEP 2: 투입 가능성 점수 계산
        if (overlappingCount >= 3) {
            return 0;   // 배제
        }
        if (overlappingCount == 2) {
            return 70;
        }
        return 100;

    }
}
