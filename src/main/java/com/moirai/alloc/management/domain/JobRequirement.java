package com.moirai.alloc.management.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
public class JobRequirement {
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "required_count")
    private int requiredCount;

    protected JobRequirement() {}

    protected JobRequirement(Long jobId, int requiredCount) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId는 필수");
        }
        if (requiredCount <= 0) {
            throw new IllegalArgumentException("인원 수는 1 이상");
        }
        this.jobId = jobId;
        this.requiredCount = requiredCount;
    }

    public Long getJobId() { return jobId; }
    public int getRequiredCount() { return requiredCount; }

    // 중복 값 생성 막는 생성자(equals, hashCode)에 대하여 추가할지 미정

}
