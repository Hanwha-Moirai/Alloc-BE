package com.moirai.alloc.management.command.dto;

import com.moirai.alloc.management.domain.entity.TechReqLevel;
import lombok.Getter;

@Getter
public class TechRequirementRequestDTO {
    private Long techId;       // TechStandard 선택
    private TechReqLevel level;
}
