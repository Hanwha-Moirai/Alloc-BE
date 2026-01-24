package com.moirai.alloc.search.command.infra.indexing;

import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill;
// 기술명 - 숙련도 쌍을 명확히 하기 위한 projection DTO, (object보다 유지보수 용이)
public record TechSkillRow(String techName,
                           EmployeeSkill.Proficiency proficiency
) {}