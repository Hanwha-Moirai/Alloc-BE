package com.moirai.alloc.management.command.service;

import com.moirai.alloc.project.command.domain.Project;

import java.time.LocalDate;

public record ProjectSpecMetadata(
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Integer predictedCost,
        String partners,
        String description,
        Project.ProjectType projectType,
        Project.ProjectStatus projectStatus
) {
}
