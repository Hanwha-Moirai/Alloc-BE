package com.moirai.alloc.meeting.command.repository;

import com.moirai.alloc.meeting.command.domain.command.domain.entity.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaCommandRepository extends JpaRepository<Agenda, Long> {
    void deleteByMeetingMeetingId(Long meetingId);

    long countByMeetingMeetingId(Long meetingId);
}
