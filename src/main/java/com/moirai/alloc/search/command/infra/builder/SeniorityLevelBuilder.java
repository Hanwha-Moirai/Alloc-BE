package com.moirai.alloc.search.command.infra.builder;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.search.query.domain.vocabulary.JobGrade;
import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;

public class SeniorityLevelBuilder {
    private SeniorityLevelBuilder() {}

    public static SeniorityLevel from(Employee employee) {
        if (employee == null || employee.getTitleStandard() == null) {
            return SeniorityLevel.JUNIOR; // 기본값 정책
        }
        JobGrade grade = JobGrade.fromTitleName(
                employee.getTitleStandard().getTitleName()
        );

        return mapFromGrade(grade);
    }
    private static SeniorityLevel mapFromGrade(JobGrade grade) {
        if (grade == null) return SeniorityLevel.JUNIOR;

        if (grade.getLevel() >= JobGrade.SENIOR_MANAGER.getLevel()) {
            return SeniorityLevel.SENIOR;
        }

        if (grade.getLevel() >= JobGrade.SENIOR_ASSOCIATE.getLevel()) {
            return SeniorityLevel.MIDDLE;
        }

        return SeniorityLevel.JUNIOR;
    }

}
