package com.moirai.alloc.meeting.command.repository;

import com.moirai.alloc.meeting.command.domain.command.domain.entity.MeetingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingRecordCommandRepository extends JpaRepository<MeetingRecord, Long> {
    Optional<MeetingRecord> findByMeetingIdAndIsDeletedFalse(Long meetingId);
}
