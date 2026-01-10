package com.moirai.alloc.profile.common.domain;

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
    @JoinColumn(name = "title_standard", nullable = false)
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

}

