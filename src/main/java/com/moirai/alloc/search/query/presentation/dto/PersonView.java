package com.moirai.alloc.search.query.presentation.dto;

import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import com.moirai.alloc.search.query.domain.vocabulary.WorkingType;
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

    // 검색 결과용 요약
    private List<String> techNames;
    private SkillLevel representativeSkillLevel;

    private Integer activeProjectCount;

    private String department;
    private WorkingType workingType;
    private SeniorityLevel seniorityLevel;

    private String experienceDomainText;
    private String profileSummary;
}
