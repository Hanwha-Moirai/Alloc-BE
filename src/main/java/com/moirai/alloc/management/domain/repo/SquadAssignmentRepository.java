package com.moirai.alloc.management.domain.repo;

import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    @Query("""
    select distinct p
    from SquadAssignment sa
    join Project p on sa.projectId = p.projectId
    where sa.userId = :userId
      and sa.finalDecision = 'ASSIGNED'
""")
    List<Project> findAssignedProjects(@Param("userId") Long userId);

    @Query("""
        select count(sa)
        from SquadAssignment sa
        join Project p on sa.projectId = p.projectId
        where sa.userId = :userId
          and sa.finalDecision = 'ASSIGNED'
          and p.projectType = :projectType
    """)
    long countAssignedProjectsByType(
            @Param("userId") Long userId,
            @Param("projectType") Project.ProjectType projectType
    );

    @Query("""
        select max(p.endDate)
        from SquadAssignment sa
        join Project p on sa.projectId = p.projectId
        where sa.userId = :userId
          and sa.finalDecision = 'ASSIGNED'
          and p.projectType = :projectType
    """)
    LocalDate findLatestAssignedProjectEndDate(
            @Param("userId") Long userId,
            @Param("projectType") Project.ProjectType projectType
    );
    // 프로젝트에 대한 모든 배정 후보 조회
    List<SquadAssignment> findByProjectId(Long projectId);

    @Query("""
    select count(sa)
    from SquadAssignment sa
    join Employee e on sa.userId = e.userId
    where sa.projectId = :projectId
      and sa.finalDecision = 'ASSIGNED'
      and e.job.jobId = :jobId
""")
    long countAssignedByProjectAndJob(
            @Param("projectId") Long projectId,
            @Param("jobId") Long jobId
    );


}
