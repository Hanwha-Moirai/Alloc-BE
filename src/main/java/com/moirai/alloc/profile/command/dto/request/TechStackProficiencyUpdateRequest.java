package com.moirai.alloc.profile.command.dto.request;

import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill.Proficiency;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TechStackProficiencyUpdateRequest {

    @NotNull
    private Proficiency proficiency;
}
