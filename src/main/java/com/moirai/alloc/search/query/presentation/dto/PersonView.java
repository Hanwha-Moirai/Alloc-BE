package com.moirai.alloc.search.query.presentation.dto;

import com.moirai.alloc.search.query.domain.model.SkillLevel;
import com.moirai.alloc.search.query.domain.model.WorkingType;
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
    private List<String> techs;

    private SkillLevel skillLevel;
    private Integer activeProjectCount;

    private String department;
    private WorkingType workingType;
}
