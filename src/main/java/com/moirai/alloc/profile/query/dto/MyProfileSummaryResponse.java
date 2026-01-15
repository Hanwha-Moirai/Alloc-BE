package com.moirai.alloc.profile.query.dto;

import lombok.Builder;
import lombok.Getter;

/* 프로필 모달 */
@Getter
@Builder
public class MyProfileSummaryResponse {

    private final String userName;

    private final String jobName;

    private final String titleName;

    private final String profileImageUrl;
}
