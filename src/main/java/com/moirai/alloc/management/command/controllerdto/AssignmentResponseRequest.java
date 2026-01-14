package com.moirai.alloc.management.command.controllerdto;

import lombok.Getter;

@Getter
public class AssignmentResponseRequest {
    private Long userId;
    private AssignmentResponseType responseType;
}