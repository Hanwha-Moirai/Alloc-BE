package com.moirai.alloc.calendar.command.repository;

import com.moirai.alloc.calendar.command.domain.entity.EventType;
import com.moirai.alloc.calendar.command.domain.entity.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventsRepository extends JpaRepository<Events, Long> {

    @Query("""
        select e
        from Events e
        where e.projectId = :projectId
          and e.deleted = false
          and e.startDate < :toEnd
          and e.endDate > :fromStart
          and (
                e.eventType in ('PUBLIC','VACATION')
                or (e.eventType = 'PRIVATE' and e.ownerUserId = :userId)
          )
        """)
    List<Events> findVisibleEvents(
            Long projectId,
            LocalDateTime fromStart,
            LocalDateTime toEnd,
            Long userId
    );

    List<Events> findByProjectIdAndDeletedFalseAndStartDateLessThanAndEndDateGreaterThan(
            Long projectId,
            LocalDateTime toEnd,
            LocalDateTime fromStart
    );

    Optional<Events> findByIdAndProjectIdAndDeletedFalse(Long eventId, Long projectId);

    List<Events> findByProjectIdAndOwnerUserIdAndEventTypeAndDeletedFalse(
            Long projectId,
            Long ownerUserId,
            EventType eventType
    );

    Optional<Events> findByProjectIdAndOwnerUserIdAndEventTypeAndEventNameAndDeletedFalse(
            Long projectId,
            Long ownerUserId,
            EventType eventType,
            String eventName
    );
}
