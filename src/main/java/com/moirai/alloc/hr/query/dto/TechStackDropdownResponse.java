package com.moirai.alloc.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/* 기술스택 드롭다운 응답 */
@Getter
@AllArgsConstructor
public class TechStackDropdownResponse {

    private List<TechStandardResponse> items;   //드롭다운에 나타나는 항목
    private boolean hasNext;                    // 다음 스크롤 확인
    private TechStackCursor nextCursor;         // 마지막 item 기반 다음 요청에 실을 커서
}
