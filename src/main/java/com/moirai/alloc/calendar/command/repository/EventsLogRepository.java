package com.moirai.alloc.calendar.command.repository;

import com.moirai.alloc.calendar.command.domain.entity.EventsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventsLogRepository extends JpaRepository<EventsLog, Long> {
}
