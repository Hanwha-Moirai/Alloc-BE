package com.moirai.alloc.management.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class TechRequirement {
    @Column(name = "tech_id")
    private Long techId;

    @Enumerated(EnumType.STRING)
    @Column(name = "req_level")
    private TechReqLevel TechLevel;
}
