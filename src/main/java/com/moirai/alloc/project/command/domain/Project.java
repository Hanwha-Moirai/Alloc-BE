package com.moirai.alloc.project.command.domain;

import com.moirai.alloc.management.domain.JobRequirement;
import com.moirai.alloc.management.domain.TechRequirement;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection
    @CollectionTable(
            name = "project_job_requirement",
            joinColumns = @JoinColumn(name = "project_id")
    )
    private List<JobRequirement> jobRequirements = new ArrayList<>();
    // 프로젝트 직무 요구 사항을 전체 교체할때 사용
    public void changeJobRequirements(List<JobRequirement> jobReq) {
        this.jobRequirements.clear();
        this.jobRequirements.addAll(jobReq);
    }

    @ElementCollection
    @CollectionTable(
            name = "project_tech_requirement",
            joinColumns = @JoinColumn(name = "project_id")
    )
    private List<TechRequirement> techRequirements = new ArrayList<>();

    public void changeTechRequirements(List<TechRequirement> techReq) {
        this.techRequirements.clear();
        this.techRequirements.addAll(techReq);
    }

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
