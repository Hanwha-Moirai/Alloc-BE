package com.moirai.alloc.search.domain.event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeSkillChangedEvent {
    //employee_skill 에 대해 INSERT, UPDATE, DELETE
    //검색 영향; 기술 목록, 숙련도, 기술 이름
    // 기술이 변경된 직원
    //검색 인덱싱은 부분 업데이트가 아니기 때문에, PersonDocumnet 재생성
    private final Long employeeId;
}
