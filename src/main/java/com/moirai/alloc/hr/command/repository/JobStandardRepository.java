package com.moirai.alloc.hr.command.repository;

import com.moirai.alloc.hr.command.domain.JobStandard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobStandardRepository extends JpaRepository<JobStandard, Long> {

    // 드롭다운 정렬
    List<JobStandard> findAllByOrderByJobNameAsc();

    boolean existsByJobNameIgnoreCase(String jobName);
}
