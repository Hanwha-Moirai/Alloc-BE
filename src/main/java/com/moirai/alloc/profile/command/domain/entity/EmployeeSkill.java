package com.moirai.alloc.profile.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import com.moirai.alloc.hr.command.domain.TechStandard;
import jakarta.persistence.*;
import lombok.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "employee_skill",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tech_id"})
)
public class EmployeeSkill extends BaseTimeEntity {

    public enum Proficiency { LV1, LV2, LV3 }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_tech_id")
    private Long employeeTechId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tech_id", nullable = false)
    private TechStandard tech;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", nullable = false)
    private Proficiency proficiency;

    @Builder
    private EmployeeSkill(TechStandard tech, Employee employee, Proficiency proficiency) {
        this.tech = tech;
        this.employee = employee;
        this.proficiency = (proficiency == null) ? Proficiency.LV1 : proficiency;
    }

    public void changeProficiency(Proficiency proficiency) {
        if (proficiency == null) {
            throw new IllegalArgumentException("PROFICIENCY_REQUIRED");
        }
        this.proficiency = proficiency;
    }

}
