package com.moirai.alloc.profile.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyTechStackResponse {
    private Long employeeTechId;
    private Long techId;
    private String techName;
    private String proficiency; // LV1~LV3
}

