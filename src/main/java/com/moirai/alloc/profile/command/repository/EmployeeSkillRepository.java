package com.moirai.alloc.profile.command.repository;

import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill;
import com.moirai.alloc.search.command.infra.indexing.TechSkillRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill,Long> {

    boolean existsByEmployee_UserIdAndTech_TechId(Long userId, Long techId);

    //
    @Query("""
        select es.tech.techName
            from EmployeeSkill es
            where es.employee.userId = :employeeId
    """)
    List<String> findTechNamesForIndexing(@Param("employeeId") Long employeeId);

    @Query("""
        select new com.moirai.alloc.search.command.infra.indexing.TechSkillRow(
                                                  es.tech.techName,
                                                  es.proficiency
                                              )
        from EmployeeSkill es
        where es.employee.userId = :employeeId
    """)
    List<TechSkillRow> findTechSkillsForIndexing(
            @Param("employeeId") Long employeeId
    );
}
