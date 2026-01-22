package com.moirai.alloc.admin.query.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserListItem {
    private final Long userId;
    private final String userName;
    private final String jobName;   //직군
    private final String email;
    private final String auth;      // ADMIN/PM/USER
    private final String deptName;  //부서
    private final String titleName; //직급
    private final String status; // ACTIVE/SUSPENDED/DELETED
}
