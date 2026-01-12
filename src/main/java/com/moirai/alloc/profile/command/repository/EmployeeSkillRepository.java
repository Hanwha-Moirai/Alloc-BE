package com.moirai.alloc.profile.command.repository;

import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill;
import com.moirai.alloc.profile.query.dto.MyTechStackResponse;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill,Long> {

    // List<EmployeeSkill> findByEmployee_UserIdOrderByTech_TechNameAsc(Long userId);
}
