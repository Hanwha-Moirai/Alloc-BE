package com.moirai.alloc.profile.query.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/* DB 그대로 받기용
* [Row1] 프로젝트A + Java
* [Row2] 프로젝트A + JPA
* [Row3] 프로젝트A + Docker
* [Row4] 프로젝트B + Kafka

* 프론트엔드로 직접 내려가지 않음
* Service에서 그룹핑 후 MyProjectHistoryResponse로 변환됨
* */
@Getter
@Builder
public class MyProjectHistoryRow {
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;

    private String myJobName; //직군

    // 기여 기술
    private Long techId;
    private String techName;
    private String proficiency; // LV1~LV3
}

