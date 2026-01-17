package com.moirai.alloc.profile.command.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MyProfileUpdateRequest {

    @Email
    private String email;
    private String phone;
    private Long jobId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}