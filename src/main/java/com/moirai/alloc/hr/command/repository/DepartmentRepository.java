package com.moirai.alloc.hr.command.repository;

import com.moirai.alloc.hr.command.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
