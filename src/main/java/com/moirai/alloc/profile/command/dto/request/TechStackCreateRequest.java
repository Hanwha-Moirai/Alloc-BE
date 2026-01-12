package com.moirai.alloc.profile.command.dto.request;

import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TechStackCreateRequest {

    @NotNull
    private Long techId;
    private EmployeeSkill.Proficiency proficiency;
}
