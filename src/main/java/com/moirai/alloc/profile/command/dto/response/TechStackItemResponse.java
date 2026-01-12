package com.moirai.alloc.profile.command.dto.response;

import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TechStackItemResponse {

    private Long employeeTechId;

    private Long techId;
    private String techName;

    private EmployeeSkill.Proficiency proficiency;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
