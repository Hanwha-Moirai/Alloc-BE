package com.moirai.alloc.search.query.domain.intent;

import com.moirai.alloc.search.query.domain.condition.ComparisonType;
import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import com.moirai.alloc.search.query.domain.vocabulary.WorkingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIntent {
    // GPT가 해석한 의미 결과; 고정된 json 구조
    private String freeText;

    private Integer activeProjectCount;
    private ComparisonType comparisonType;

    // 기술 관련 의미
    private SkillLevel skillLevel; // 레벨
    private List<String> techName; // 자바, 파이썬 등

    //근무/조직
    private WorkingType workingType;
    private SeniorityLevel seniorityLevel;

    //직무, 조직
    private String jobTitle;
    private String department;

    //응답 갯수 제한
    private Integer limit;
}