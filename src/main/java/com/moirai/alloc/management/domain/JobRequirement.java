package com.moirai.alloc.management.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class JobRequirement {
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "required_count")
    private int requiredCount;

    protected JobRequirement() {}

}
