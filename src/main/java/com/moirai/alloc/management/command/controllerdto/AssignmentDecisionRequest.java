package com.moirai.alloc.management.command.controllerdto;

import com.moirai.alloc.management.domain.entity.FinalDecision;
import lombok.Getter;

@Getter
public class AssignmentDecisionRequest {
    private Long pmUserId;
    private FinalDecision decision;
}