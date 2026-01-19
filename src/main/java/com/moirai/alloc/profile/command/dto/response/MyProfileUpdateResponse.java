package com.moirai.alloc.profile.command.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyProfileUpdateResponse {

    private Long userId;

    private String userName;
    private String email;
    private String phone;

    private java.time.LocalDate birthday;

    private Long jobId;
    private String jobName;

    private Long titleId;
    private String titleName;
}
