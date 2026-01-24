package com.moirai.alloc.search.infra.builder;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.search.query.domain.model.SeniorityLevel;

public class SeniorityLevelBuilder {
    //TODO : 직급체계 정하고, 이 부분 수정 필요

    private SeniorityLevelBuilder() {}

    public static SeniorityLevel from(Employee employee) {
        if (employee == null || employee.getTitleStandard() == null) {
            return SeniorityLevel.JUNIOR; // 기본값 정책
        }
        return mapFromTitle(employee.getTitleStandard().getTitleName());
    }
    public static SeniorityLevel mapFromTitle(String titleName) {
        if (titleName == null || titleName.isBlank()) {
            return SeniorityLevel.JUNIOR;
        }
        if(isSenior(titleName)){
            return SeniorityLevel.SENIOR;
        }
        if(isMiddle(titleName)){
            return SeniorityLevel.MIDDLE;
        }
        return SeniorityLevel.JUNIOR;
    }

    // NOTE:
// - titleName은 사내 직급 표준값(title_standard.title_name)만 들어온다고 가정
// - 자연어("시니어급") 처리는 검색 파싱 단계(GPT/Rule)에서 수행


    private static boolean isSenior(String titleName) {
        return titleName.equals("부장")
                || titleName.equals("차장")
                || titleName.equals("임원");
    }

    private static boolean isMiddle(String titleName) {
        return titleName.equals("과장")
                || titleName.equals("대리");
    }

}
