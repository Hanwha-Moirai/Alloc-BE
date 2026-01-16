package com.moirai.alloc.profile.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/* MyProjectHistoryRow 목록을
 * projectId 기준으로 Service 계층에서 그룹핑하여 생성*/

@Getter
@Builder
@AllArgsConstructor
public class MyProjectHistoryResponse {
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;

    private String myJobName; // 직군

    private List<ContributedTech> contributedTechs;     // 기여 기술

    @Getter
    @AllArgsConstructor
    public static class ContributedTech {
        private Long techId;
        private String techName;
        private String proficiency; // LV1~LV3
    }
}

