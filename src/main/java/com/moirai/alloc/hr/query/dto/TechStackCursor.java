package com.moirai.alloc.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/* 기술 스택 드롭다운 스크롤 커서 */
@Getter
@AllArgsConstructor
public class TechStackCursor {
    private String cursorTechName;
    private Long cursorTechId;

    public static TechStackCursor empty() {
        return new TechStackCursor(null, null);
    }

    //cursorTechName/cursorTechId는 "마지막으로 받은 항목"
    public boolean isEmpty() {
        return cursorTechName == null || cursorTechName.isBlank() || cursorTechId == null;
    }
}
