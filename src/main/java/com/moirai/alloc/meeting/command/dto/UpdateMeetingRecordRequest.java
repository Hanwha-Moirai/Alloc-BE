package com.moirai.alloc.meeting.command.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateMeetingRecordRequest(
        Long meetingId,
        Double progress,
        LocalDateTime meetingDate,
        LocalDateTime meetingTime,
        List<AgendaRequest> agendas,
        List<ParticipantRequest> participants
) {
}
