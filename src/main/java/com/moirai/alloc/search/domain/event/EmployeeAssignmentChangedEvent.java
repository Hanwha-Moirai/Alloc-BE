package com.moirai.alloc.search.domain.event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeAssignmentChangedEvent {
    // 검색에 쓰일 정보; 현재 투입 여부, 최근 프로젝트 타입, 프로젝트 수, 가용성 판단
    //DB 기준; squad_assignment에서 proposed, accepted, final_decision, decided-at

}
