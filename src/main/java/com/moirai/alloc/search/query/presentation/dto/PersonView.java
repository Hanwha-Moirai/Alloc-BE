package com.moirai.alloc.search.query.presentation.dto;

import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PersonView {
    //API 응답 형태 정의; json으로 나갈 데이터 구조.dto
    private Long personId;
    private String name;
    private String jobTitle;

    // 기술 요약
    private List<String> techNames;
    private SkillLevel representativeSkillLevel;

    //상태 정보
    private Integer activeProjectCount;
    private String department;

    private String profileSummary;
}
