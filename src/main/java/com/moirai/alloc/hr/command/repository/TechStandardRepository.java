package com.moirai.alloc.hr.command.repository;

import com.moirai.alloc.hr.command.domain.TechStandard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechStandardRepository extends JpaRepository<TechStandard, Long> {
}
