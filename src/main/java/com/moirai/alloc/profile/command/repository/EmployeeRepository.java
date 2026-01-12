package com.moirai.alloc.profile.command.repository;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
}
