package com.moirai.alloc.search.infra.indexing;

import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill;

public record TechSkillRow(String techName,
                           EmployeeSkill.Proficiency proficiency
) {}