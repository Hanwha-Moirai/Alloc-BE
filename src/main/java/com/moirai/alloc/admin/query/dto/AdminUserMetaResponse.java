package com.moirai.alloc.admin.query.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminUserMetaResponse {

    private List<CodeLabel> employeeTypes;  //근무형태 (ex :FULL_TIME → 정규직 )

    private List<CodeLabel> auths; // 권한(ADMIN, USER, PM)

    private List<CodeLabel> statuses;   // 계정 상태 (ACTIVE, SUSPENDED, DELETED)

    @Getter
    @Builder
    public static class CodeLabel {
        private String code;   // enum name (FULL_TIME)
        private String label;  // 화면 표시용 (정규직)
    }
}

