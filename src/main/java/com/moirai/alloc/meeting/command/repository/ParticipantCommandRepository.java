package com.moirai.alloc.meeting.command.repository;

import com.moirai.alloc.meeting.command.domain.command.domain.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantCommandRepository extends JpaRepository<Participant, Long> {
    void deleteByMeetingMeetingId(Long meetingId);

    long countByMeetingMeetingId(Long meetingId);
}
