package com.moirai.alloc.management.query.dto.selectedList;


import lombok.Getter;

import java.util.Map;

@Getter
public class AssignmentStatusDTO {

    // 직군별 부족 인원
    private final Map<Long, Integer> shortageByJobId;

    public AssignmentStatusDTO(Map<Long, Integer> shortageByJobId) {
        this.shortageByJobId = shortageByJobId;
    }

    // 부족한 직군이 하나라도 있는지
    public boolean hasShortage() {
        return shortageByJobId.values()
                .stream()
                .anyMatch(count -> count > 0);
    }

    public int getShortage(Long jobId) {
        return shortageByJobId.getOrDefault(jobId, 0);
    }
}
