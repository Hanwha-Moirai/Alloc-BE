package com.moirai.alloc.search.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeProfileChangedEvent {
    // 어떤 일이 일어났는지만 표현(EVENT는 CONTRACT이다)
    // 검색 인덱싱 대상 식별자
    /*
    users.user_name, employee.job_id,
    department, title_standard,
    employee_type, hiring_date 중
    검색에 영향을 주는 값 변경시
     */
    private final Long employeeId;

}
