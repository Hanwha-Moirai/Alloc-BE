package com.moirai.alloc.admin.query.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AdminUserDetailResponse {

    // user
    private Long userId;
    private String loginId;
    private String userName;
    private String email;
    private String phone;
    private LocalDate birthday;
    private String auth;
    private String status;
    private String profileImg;

    // employee
    private Long jobId;
    private Long titleId;
    private Long deptId;
    private String employeeType;
    private LocalDate hiringDate;
}
