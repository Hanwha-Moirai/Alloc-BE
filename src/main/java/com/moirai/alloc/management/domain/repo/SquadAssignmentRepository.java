package com.moirai.alloc.management.domain.repo;

import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SquadAssignmentRepository extends JpaRepository<SquadAssignment, Long> {

    @Query("""
            select distinct p
            from SquadAssignment sa
            join Project p on sa.projectId = p.projectId
            where sa.userId = :userId
            """)
    List<Project> findProjectsByUserId(Long userId);


    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
