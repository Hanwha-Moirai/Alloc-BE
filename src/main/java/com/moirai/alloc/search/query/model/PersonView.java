package com.moirai.alloc.search.query.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PersonView {
    // 인력 리스트 돌려줄 dto
    // 검색 결과를 화면/응답 전용 모델로 정리
    private Long personId;
    private String name;
    private String jobTitle;
    private List<String> techs;

    private SkillLevel skillLevel;
    private Integer activeProjectCount;

    private String department;
    private WorkingType workingType;
}
