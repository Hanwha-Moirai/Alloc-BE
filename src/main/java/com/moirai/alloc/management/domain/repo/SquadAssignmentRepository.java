package com.moirai.alloc.management.domain.repo;

import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SquadAssignmentRepository extends CrudRepository<SquadAssignment, Long> {
    boolean existsByProjectIdAndUserIdAndFinalDecision(Long projectId, Long aLong, FinalDecision finalDecision);

    List<SquadAssignment> findByProjectIdAndFinalDecision(Long projectId, FinalDecision finalDecision);

    /** memberUserIds가 프로젝트 ASSIGNED 멤버인지 검증용 */
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
}
