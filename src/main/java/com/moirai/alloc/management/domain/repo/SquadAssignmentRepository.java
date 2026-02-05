package com.moirai.alloc.management.domain.repo;

import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface SquadAssignmentRepository extends JpaRepository<SquadAssignment, Long> {

    @Query("""
            select distinct p
            from SquadAssignment sa
            join Project p on sa.projectId = p.projectId
            where sa.userId = :userId
            """)
    Page<Project> findProjectsByUserId(@Param("userId") Long userId, Pageable pageable);


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

    boolean existsByUserIdAndFinalDecision(Long userId, FinalDecision finalDecision);

    @Query("""
    select sa.userId
    from SquadAssignment sa
    where sa.finalDecision = :decision
""")
    Set<Long> findUserIdsByFinalDecision(@Param("decision") FinalDecision decision);
    @Query("""
    select sa.userId
    from SquadAssignment sa
    where sa.projectId = :projectId
      and sa.finalDecision = :finalDecision
      and sa.userId in :userIds
""")
    List<Long> findUserIdsInProjectByDecision(
            @Param("projectId") Long projectId,
            @Param("finalDecision") FinalDecision finalDecision,
            @Param("userIds") List<Long> userIds
    );
    boolean existsByProjectIdAndUserIdAndFinalDecision(
            Long projectId,
            Long userId,
            FinalDecision finalDecision
    );
    @Query("""
        select sa
        from SquadAssignment sa
        where sa.projectId = :projectId
          and sa.finalDecision = 'ASSIGNED'
    """)
    List<SquadAssignment> findAssignedByProjectId(@Param("projectId") Long projectId);

    List<SquadAssignment> findByUserIdAndAssignmentStatus(
            Long userId,
            AssignmentStatus status
    );

    @Query("""
        select count(sa)
        from SquadAssignment sa
        join Project p on sa.projectId = p.projectId
        where sa.userId = :employeeId
          and sa.finalDecision = 'ASSIGNED'
          and p.projectStatus = 'ACTIVE'
    """)
    int countActiveProjects(@Param("employeeId") Long employeeId);

    @Query("""
        select distinct p.projectType
        from SquadAssignment sa
        join Project p on sa.projectId = p.projectId
        where sa.userId = :employeeId
          and sa.finalDecision = 'ASSIGNED'
    """)
    List<Project.ProjectType> findExperiencedProjectTypes(
            @Param("employeeId") Long employeeId
    );

    @Query("""
    select distinct p.name
    from SquadAssignment sa
    join Project p on sa.projectId = p.projectId
    where sa.userId = :employeeId
      and sa.finalDecision = 'ASSIGNED'
""")
    List<String> findExperiencedProjectTitles(
            @Param("employeeId") Long employeeId
    );



}