package com.moirai.alloc.hr.command.repository;

import com.moirai.alloc.hr.command.domain.JobStandard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobStandardRepository extends JpaRepository<JobStandard, Long> {

    // 드롭다운 정렬
    List<JobStandard> findAllByOrderByJobNameAsc();

    boolean existsByJobNameIgnoreCase(String jobName);

    //생성 중복 체크
    @Query("""
    SELECT (COUNT(j) > 0)
    FROM JobStandard j
    WHERE LOWER(REPLACE(j.jobName, ' ', '')) = :norm
""")
    boolean existsByJobNameNorm(@Param("norm") String norm);

    //수정 중복 체크
    @Query("""
    SELECT (COUNT(j) > 0)
    FROM JobStandard j
    WHERE LOWER(REPLACE(j.jobName, ' ', '')) = :norm
      AND j.jobId <> :jobId
""")
    boolean existsByJobNameNormAndJobIdNot(@Param("norm") String norm, @Param("jobId") Long jobId);

}
