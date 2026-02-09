package com.moirai.alloc.notification.command.repository;

import com.moirai.alloc.notification.command.domain.entity.AlarmLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AlarmLogRepository extends JpaRepository<AlarmLog, Long> {

    Page<AlarmLog> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<AlarmLog> findByUserIdAndDeletedFalseAndIdGreaterThan(Long userId, Long id, Pageable pageable);

    long countByUserIdAndReadFalseAndDeletedFalse(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update AlarmLog a
       set a.read = true,
           a.updatedAt = CURRENT_TIMESTAMP
     where a.userId = :userId
       and a.deleted = false
       and a.read = false
    """)
    int markAllRead(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update AlarmLog a
        set a.read = true,
           a.updatedAt = CURRENT_TIMESTAMP
        where a.id = :alarmId
       and a.userId = :userId
       and a.deleted = false
    """)
    int markRead(@Param("userId") Long userId, @Param("alarmId") Long alarmId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update AlarmLog a
       set a.deleted = true,
           a.updatedAt = CURRENT_TIMESTAMP
     where a.id = :alarmId
       and a.userId = :userId
       and a.deleted = false
    """)
    int softDeleteOne(@Param("userId") Long userId, @Param("alarmId") Long alarmId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update AlarmLog a
       set a.deleted = true,
           a.updatedAt = CURRENT_TIMESTAMP
     where a.userId = :userId
       and a.deleted = false
       and a.read = true
    """)
    int softDeleteAllRead(@Param("userId") Long userId);
}
