package com.moirai.alloc.profile.command.repository;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    // 직군별 직원 조회
    @Query("""
        select e
        from Employee e
        where e.job.jobId = :jobId
    """)
    List<Employee> findByJobId(@Param("jobId") Long jobId);

    List<Employee> findAllByUserIdIn(Collection<Long> userIds);

    // 검색 대상 ID
    @Query("""
        select e.userId
        from Employee e
        join e.user u
            where u.status = com.moirai.alloc.user.command.domain.User.Status.ACTIVE
    """)
    List<Long> findAllIdsForIndexing();

    // 인덱싱용 기본
    @Query("""
select distinct e
from Employee e
join fetch e.user u
join fetch e.job j
join fetch e.department d
join fetch e.titleStandard ts
where e.userId = :employeeId
""")
    Optional<Employee> findByIdForIndexing(
            @Param("employeeId") Long employeeId
    );




}
