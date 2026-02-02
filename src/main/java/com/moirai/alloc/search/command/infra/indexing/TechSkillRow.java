package com.moirai.alloc.search.command.infra.indexing;

import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill;

public class TechSkillRow {

    private final String techName;
    private final EmployeeSkill.Proficiency proficiency;

    public TechSkillRow(
            String techName,
            EmployeeSkill.Proficiency proficiency
    ) {
        this.techName = techName;
        this.proficiency = proficiency;
    }

    public String getTechName() {
        return techName;
    }

    public EmployeeSkill.Proficiency getProficiency() {
        return proficiency;
    }
}