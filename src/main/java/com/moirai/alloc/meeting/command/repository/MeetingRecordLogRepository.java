package com.moirai.alloc.meeting.command.repository;

import com.moirai.alloc.meeting.command.domain.command.domain.entity.MeetingRecordLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRecordLogRepository extends JpaRepository<MeetingRecordLog, Long> {
    List<MeetingRecordLog> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);
}
