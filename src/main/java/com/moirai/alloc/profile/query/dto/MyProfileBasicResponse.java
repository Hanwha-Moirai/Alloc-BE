package com.moirai.alloc.profile.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class MyProfileBasicResponse {
    private Long userId;
    private String userName;
    private LocalDate birthday;
    private String email;
    private String phone;

    private Long jobId;             // nullable
    private String jobName;         // nullable
    private Long deptId;
    private String deptName;
    private String employeeType;
    private Long titleStandardId;
    private String titleName;
    private LocalDate hiringDate;

    private boolean assignedNow;    // 투입중 여부 (ACTIVE 프로젝트 + ASSIGNED)
}

