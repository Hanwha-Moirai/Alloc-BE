package com.moirai.alloc.management.command.dto;

import lombok.Getter;

import java.util.List;

// 프론트에서 "선택한 유저들" 저장용
@Getter
public class AssignUsersCommandDTO {
    private List<Long> userIds;
}