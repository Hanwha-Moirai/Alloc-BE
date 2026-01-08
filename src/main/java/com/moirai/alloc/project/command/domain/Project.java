package com.moirai.alloc.project.command.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "project")
public class Project {

    public enum ProjectStatus { DRAFT, ACTIVE, CLOSED, HOLD }
    public enum ProjectType { NEW, OPERATION, MAINTENANCE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false)
    private ProjectStatus projectStatus;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "predicted_cost")
    private Integer predictedCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false)
    private ProjectType projectType;

    @Column(name = "partners", length = 50)
    private String partners;

    @Builder
    private Project(String name,
                    LocalDate startDate,
                    LocalDate endDate,
                    ProjectStatus projectStatus,
                    String description,
                    Integer predictedCost,
                    ProjectType projectType,
                    String partners) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.predictedCost = predictedCost;
        this.partners = partners;

        this.projectStatus = (projectStatus == null) ? ProjectStatus.DRAFT : projectStatus;
        this.projectType = (projectType == null) ? ProjectType.NEW : projectType;
    }

}
