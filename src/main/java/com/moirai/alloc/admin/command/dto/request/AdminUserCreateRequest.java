package com.moirai.alloc.admin.command.dto.request;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.user.command.domain.User;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AdminUserCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String loginId;

    @NotBlank
    @Size(min = 10, max = 255)
    private String password;

    @NotBlank
    @Size(max = 40)
    private String userName;

    @NotNull
    private LocalDate birthday;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotNull
    private User.Auth auth;

    @NotNull
    private Long jobId;

    @NotNull
    private Long deptId;

    @NotNull
    private Long titleStandardId;

    private String profileImg;

    @NotNull
    private Employee.EmployeeType employeeType;

    @NotNull
    private LocalDate hiringDate;
}

