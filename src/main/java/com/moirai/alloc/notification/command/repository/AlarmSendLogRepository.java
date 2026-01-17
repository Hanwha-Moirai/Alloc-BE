package com.moirai.alloc.notification.command.repository;

import com.moirai.alloc.notification.command.domain.entity.AlarmSendLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmSendLogRepository extends JpaRepository<AlarmSendLog, Long> {
    Page<AlarmSendLog> findAllByOrderBySentAtDesc(Pageable pageable);
}
