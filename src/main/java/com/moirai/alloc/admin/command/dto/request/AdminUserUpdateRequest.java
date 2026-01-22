package com.moirai.alloc.admin.command.dto.request;

import com.moirai.alloc.profile.command.domain.entity.Employee.EmployeeType;
import com.moirai.alloc.user.command.domain.User;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AdminUserUpdateRequest {

    private String password;
    private String userName;
    @Email
    private String email;
    private String phone;
    private LocalDate birthday;
    private String profileImg;

    private Long jobId;
    private Long deptId;
    private Long titleStandardId;
    private EmployeeType employeeType;

    private User.Auth auth;
    private User.Status status;
}

