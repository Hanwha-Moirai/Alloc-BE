package com.moirai.alloc.profile.command.domain.entity;

import com.moirai.alloc.hr.command.domain.Department;
import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.domain.TitleStandard;
import com.moirai.alloc.user.command.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "employee")
public class Employee {


    public enum EmployeeType { FULL_TIME, CONTRACT, INTERN, VENDOR }

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobStandard job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "title_standard_id", nullable = false)
    private TitleStandard titleStandard;

    @Column(name = "project_no")
    private Integer projectNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false)
    private EmployeeType employeeType;

    @Column(name = "hiring_date", nullable = false)
    private LocalDate hiringDate;

    @Builder
    private Employee(User user,
                     JobStandard job,
                     Department department,
                     TitleStandard titleStandard,
                     Integer projectNo,
                     EmployeeType employeeType,
                     LocalDate hiringDate) {
        this.user = user;
        this.job = job;
        this.department = department;
        this.titleStandard = titleStandard;
        this.projectNo = projectNo;
        this.hiringDate = hiringDate;

        this.employeeType = (employeeType == null) ? EmployeeType.FULL_TIME : employeeType;
    }
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private List<EmployeeSkill> skills = new ArrayList<>();

    //스킬 조회용
    public int getSkillLevel(Long techId) {
        for (EmployeeSkill skill : this.skills) {
            if (skill.getTech().getTechId().equals(techId)) {
                return skill.getProficiency().ordinal() + 1;
            }
        }
        return 0; // 기술 없으면 0
    }
    // 경험한 기술 조회용
    public Set<Long> getExperiencedTechIds() {
        return this.skills.stream()
                .map(skill -> skill.getTech().getTechId())
                .collect(Collectors.toSet());
    }



    public void changeJob(JobStandard job) {
        this.job = job;
    }

}

