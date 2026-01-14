package com.moirai.alloc.management.domain.policy.scoring.calculator.experience.fit;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.profile.common.domain.Employee;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
@Component
@RequiredArgsConstructor
public class RecencyExperiencePolicy {
    // 최근에 비슷한 프로젝트를 했는가?
    private static final int SCORE_WITHIN_6_MONTHS = 20;
    private static final int SCORE_WITHIN_12_MONTHS = 10;

    private final SquadAssignmentRepository assignmentRepository;

    public int score(Project project, Employee employee) {

        LocalDate latestEndDate =
                assignmentRepository.findLatestAssignedProjectEndDate(
                        employee.getUserId(),
                        project.getProjectType()
                );

        if (latestEndDate == null) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        long monthsBetween =
                ChronoUnit.MONTHS.between(latestEndDate, today);

        if (monthsBetween <= 6) {
            return SCORE_WITHIN_6_MONTHS;
        }
        if (monthsBetween <= 12) {
            return SCORE_WITHIN_12_MONTHS;
        }
        return 0;
    }

}
