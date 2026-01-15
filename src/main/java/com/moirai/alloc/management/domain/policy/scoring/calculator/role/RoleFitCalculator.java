package com.moirai.alloc.management.domain.policy.scoring.calculator.role;


import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Component;

@Component
public class RoleFitCalculator {
    public int calculate(Project project, Employee employee) {
        Long employeeJobId = employee.getJob().getJobId();

        boolean matched = project.getJobRequirements().stream()
                .anyMatch(req ->
                        req.getJobId().equals(employeeJobId)
                );

        return matched ? 100 : 0;
    }
}
