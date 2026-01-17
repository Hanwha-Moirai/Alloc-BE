package com.moirai.alloc.management.query.dto.selectedList;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssignmentSummaryCardDTO {

    private int totalSelected;     // 총 후보 수
    private int requestedCount;    // REQUESTED (응답 대기)
    private int acceptedCount;     // ACCEPTED (수락)
    private int interviewCount;    // INTERVIEW_REQUESTED (면담 요청)
}
