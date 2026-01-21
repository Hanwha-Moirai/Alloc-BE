package com.moirai.alloc.project.command.domain;

import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.domain.vo.TechRequirement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
    //프로젝트 수정
    public void updateBasicInfo(
            String name,
            LocalDate startDate,
            LocalDate endDate,
            String partners,
            String description,
            Integer predictedCost
    ) {
        validatePeriod(startDate, endDate);
        validatePredictedCost(predictedCost);

        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.partners = partners;
        this.description = description;
        this.predictedCost = predictedCost;
    }
    public void changeProjectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
    }
    public void changeProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("프로젝트 기간은 필수입니다.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다.");
        }
    }

    private void validatePredictedCost(Integer cost) {
        if (cost == null || cost < 0) {
            throw new IllegalArgumentException("예산은 0 이상이어야 합니다.");
        }
    }



}
