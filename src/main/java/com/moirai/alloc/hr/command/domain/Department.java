package com.moirai.alloc.hr.command.domain;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import com.moirai.alloc.user.command.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "department")
public class Department extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "dept_name", nullable = false, length = 100)
    private String deptName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_dept_id")
    private Department parentDept;

    @Builder
    private Department(String deptName, User manager, Boolean isActive, Department parentDept) {
        this.deptName = deptName;
        this.manager = manager;
        this.isActive = (isActive == null) ? Boolean.TRUE : isActive;
        this.parentDept = parentDept;
    }

}

