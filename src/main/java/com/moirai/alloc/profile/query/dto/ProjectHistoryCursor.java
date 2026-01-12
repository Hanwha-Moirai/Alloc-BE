package com.moirai.alloc.profile.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/* 프로젝트 히스토리 무한스크롤에서 다음 요청을 위한 커서 키 */
@Getter
@AllArgsConstructor
public class ProjectHistoryCursor {
    private LocalDate cursorEndDate;
    private Long cursorProjectId;
}
