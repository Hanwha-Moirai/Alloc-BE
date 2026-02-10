package com.moirai.alloc.calendar.command.repository;

import com.moirai.alloc.calendar.command.domain.entity.EventType;
import com.moirai.alloc.calendar.command.domain.entity.Events;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
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

    @Query("""
        select count(e)
        from Events e
        where e.deleted = false
          and e.startDate < :toExclusive
          and e.endDate > :fromStart
          and e.projectId in (
                select sa.projectId
                from SquadAssignment sa
                where sa.userId = :userId
                  and sa.finalDecision = :decision
          )
          and (
              (
                e.eventType = com.moirai.alloc.calendar.command.domain.entity.EventType.PUBLIC
                and exists (
                    select 1
                    from PublicEventsMember pem
                    where pem.eventId = e.id
                      and pem.userId = :userId
                )
              )
              or (
                (e.eventType = com.moirai.alloc.calendar.command.domain.entity.EventType.PRIVATE
                                  or e.eventType = com.moirai.alloc.calendar.command.domain.entity.EventType.VACATION
                                  or e.eventType is null)
                and e.ownerUserId = :userId
              )
          )
        """)
    long countWeeklyVisibleEventsAcrossMyProjects(
            Long userId,
            com.moirai.alloc.management.domain.entity.FinalDecision decision,
            LocalDateTime fromStart,
            LocalDateTime toExclusive
    );

    @Query("""
        select e
        from Events e
        where e.deleted = false
          and e.startDate < :toExclusive
          and e.endDate > :fromStart
          and e.projectId in (
                select sa.projectId
                from SquadAssignment sa
                where sa.userId = :userId
                  and sa.finalDecision = :decision
          )
          and (
                :cursorStart is null
                or e.startDate > :cursorStart
                or (e.startDate = :cursorStart and e.id > :cursorId)
          )
          and (
              (
                e.eventType = com.moirai.alloc.calendar.command.domain.entity.EventType.PUBLIC
                and exists (
                    select 1
                    from PublicEventsMember pem
                    where pem.eventId = e.id
                      and pem.userId = :userId
                )
              )
              or (
                e.eventType = com.moirai.alloc.calendar.command.domain.entity.EventType.PRIVATE
                and e.ownerUserId = :userId
              )
              or (
                e.eventType = com.moirai.alloc.calendar.command.domain.entity.EventType.VACATION
                and e.ownerUserId = :userId
              )
          )
        order by e.startDate asc, e.id asc
        """)
    List<Events> findTodayVisibleEventsAcrossMyProjects(
            @Param("userId") Long userId,
            @Param("decision") FinalDecision decision,
            @Param("fromStart") LocalDateTime fromStart,
            @Param("toExclusive") LocalDateTime toExclusive,
            @Param("cursorStart") LocalDateTime cursorStart,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
        select e
        from Events e
        where e.deleted = false
          and e.projectId = :projectId
          and e.startDate >= :now
          and (
                :cursorStart is null
                or e.startDate > :cursorStart
                or (e.startDate = :cursorStart and e.id > :cursorId)
          )
          and (
              (
                e.eventType = com.moirai.alloc.calendar.command.domain.entity.EventType.PUBLIC
                and exists (
                    select 1
                    from PublicEventsMember pem
                    where pem.eventId = e.id
                      and pem.userId = :userId
                )
              )
              or (
                e.eventType = com.moirai.alloc.calendar.command.domain.entity.EventType.PRIVATE
                and e.ownerUserId = :userId
              )
              or (
                e.eventType = com.moirai.alloc.calendar.command.domain.entity.EventType.VACATION
                and e.ownerUserId = :userId
              )
          )
        order by e.startDate asc, e.id asc
        """)
    List<Events> findUpcomingVisibleEventsInProject(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            @Param("cursorStart") LocalDateTime cursorStart,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
