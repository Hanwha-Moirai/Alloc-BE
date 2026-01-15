package com.moirai.alloc.profile.command.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyProfileUpdateRequest {

    @Email
    private String email;
    private String phone;
    private Long jobId;
}