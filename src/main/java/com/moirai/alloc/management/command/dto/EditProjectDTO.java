package com.moirai.alloc.management.command.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
public class EditProjectDTO {
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String partners;
    private String description;
    private Integer predictedCost;

}
