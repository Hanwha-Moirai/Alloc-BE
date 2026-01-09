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
    private TechReqLevel techLevel;

    protected TechRequirement() {
    }
    public TechRequirement(Long techId, TechReqLevel techLevel) {
        if (techId == null) throw new IllegalArgumentException("techId 필수");
        if (techLevel ==null) throw new IllegalArgumentException("level 선택 필수");
        this.techId = techId;
        this.techLevel = techLevel;
    }
}
