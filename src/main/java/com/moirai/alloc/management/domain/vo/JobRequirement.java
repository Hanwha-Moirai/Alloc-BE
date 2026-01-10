package com.moirai.alloc.management.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class JobRequirement {
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "required_count", nullable = false)
    private int requiredCount;

    protected JobRequirement() {}

    public JobRequirement(Long jobId, int requiredCount) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId는 필수");
        }
        if (requiredCount <= 0) {
            throw new IllegalArgumentException("인원 수는 1 이상");
        }
        this.jobId = jobId;
        this.requiredCount = requiredCount;
    }

    // 중복 값 생성 막는 생성자(equals, hashCode)에 대하여 추가할지 미정

}
