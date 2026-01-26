package com.moirai.alloc.search.query.infra.openSearch;

import com.moirai.alloc.search.query.domain.vocabulary.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDocument {
    // 이 사람이 가진 <이름,직무명, 숙련도..etc>
    // 검색 결과 원본
    // opensearch 문서 구조 그대로 표현
    // 검색 조건과 매칭되는 대상, 저장되는 한 사람의 정보
    // 검색 인덱스 DTO; 검색에 필요한 정보만 반영
    // OpenSearch에 저장되는 사람 한 명의 검색용 데이터 구조
    private Long personId;
    private String name;

    private String jobTitle;
    private String department;

    private Map<String, SkillLevel> techSkills;  // 의미 검색용
    private Map<String, Integer> techSkillNumericLevels;    // 범위 검색용

    private Integer activeProjectCount;
    private Integer seniorityLevelLevel;
    private Integer jobGradeLevel;

    private String profileSummary; // 직원 한 문자 설명, experienceDomainText의 놓친 것 커버 (낮은 가중치)

}
