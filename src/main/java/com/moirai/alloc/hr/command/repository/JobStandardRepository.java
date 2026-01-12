package com.moirai.alloc.hr.command.repository;

import com.moirai.alloc.hr.command.domain.JobStandard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStandardRepository extends JpaRepository<JobStandard, Long> {
}
