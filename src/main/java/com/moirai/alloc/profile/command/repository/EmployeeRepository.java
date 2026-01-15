package com.moirai.alloc.profile.command.repository;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    @Query("""
        select e
        from Employee e
        where e.job.jobId = :jobId
    """)
    List<Employee> findByJobId(@Param("jobId") Long jobId);
}
