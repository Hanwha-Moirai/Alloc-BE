package com.moirai.alloc.profile.command.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TechStackDeleteResponse {
    private Long employeeTechId;
    private boolean deleted;
}
