package com.moirai.alloc.admin.command.dto.response;

import com.moirai.alloc.user.command.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AdminUserResponse {
    private Long userId;

    private String loginId;
    private String userName;
    private LocalDate birthday;
    private String email;
    private String phone;
    private String profileImg;

    private User.Auth auth;
    private User.Status status;

    private Long jobId;
    private String jobName;

    private Long deptId;
    private String deptName;

    private Long titleStandardId;
    private String titleName;
}
