package com.moirai.alloc.meeting.command.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.meeting.command.dto.request.AgendaRequest;
import com.moirai.alloc.meeting.command.dto.request.CreateMeetingRecordRequest;
import com.moirai.alloc.meeting.command.dto.request.ParticipantRequest;
import com.moirai.alloc.meeting.command.dto.request.UpdateMeetingRecordRequest;
import com.moirai.alloc.meeting.command.repository.AgendaCommandRepository;
import com.moirai.alloc.meeting.command.repository.MeetingRecordCommandRepository;
import com.moirai.alloc.meeting.command.repository.ParticipantCommandRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Sql(scripts = "/sql/meeting/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
//@Sql(scripts = "/sql/meeting/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class MeetingRecordCommandServiceTest {

    private static final Long PROJECT_ID = 88001L;
    private static final Long MEETING_ID = 88001L;
    private static final Long PM_USER_ID = 88001L;
    private static final Long MEMBER_USER_ID = 88002L;

    @Autowired
    private MeetingRecordCommandService meetingRecordCommandService;

    @Autowired
    private MeetingRecordCommandRepository meetingRecordCommandRepository;

    @Autowired
    private AgendaCommandRepository agendaCommandRepository;

    @Autowired
    private ParticipantCommandRepository participantCommandRepository;

    @Test
    void createMeetingRecord_savesMeetingAgendaParticipants() {
        CreateMeetingRecordRequest request = new CreateMeetingRecordRequest(
                PROJECT_ID,
                20.0,
                LocalDateTime.of(2025, 1, 10, 10, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0),
                List.of(new AgendaRequest("주제", "내용", "결정", "TYPE1")),
                List.of(new ParticipantRequest(MEMBER_USER_ID, false))
        );

        UserPrincipal principal = new UserPrincipal(
                PM_USER_ID,
                "pm_88001",
                "pm88001@example.com",
                "PM",
                "PM",
                "pw"
        );

        Long meetingId = meetingRecordCommandService.createMeetingRecord(request, principal);

        assertThat(meetingRecordCommandRepository.findByMeetingIdAndIsDeletedFalse(meetingId)).isPresent();
        assertThat(agendaCommandRepository.findAll().stream()
                .anyMatch(agenda -> agenda.getMeeting().getMeetingId().equals(meetingId))).isTrue();
        assertThat(participantCommandRepository.findAll().stream()
                .anyMatch(participant -> participant.getMeeting().getMeetingId().equals(meetingId))).isTrue();
    }

    @Test
    void updateMeetingRecord_replacesAgendasAndParticipants() {
        UpdateMeetingRecordRequest request = new UpdateMeetingRecordRequest(
                MEETING_ID,
                55.0,
                LocalDateTime.of(2025, 1, 12, 10, 0),
                LocalDateTime.of(2025, 1, 12, 10, 0),
                List.of(new AgendaRequest("변경", "수정", "결론", "TYPE2")),
                List.of(new ParticipantRequest(PM_USER_ID, true))
        );

        UserPrincipal principal = new UserPrincipal(
                PM_USER_ID,
                "pm_88001",
                "pm88001@example.com",
                "PM",
                "PM",
                "pw"
        );

        meetingRecordCommandService.updateMeetingRecord(request, principal);

        assertThat(agendaCommandRepository.findAll().stream()
                .anyMatch(agenda -> agenda.getDiscussionTitle().equals("변경"))).isTrue();
        assertThat(participantCommandRepository.findAll().stream()
                .anyMatch(participant -> participant.getUser().getUserId().equals(PM_USER_ID))).isTrue();
    }

    @Test
    void deleteMeetingRecord_marksDeleted() {
        UserPrincipal principal = new UserPrincipal(
                PM_USER_ID,
                "pm_88001",
                "pm88001@example.com",
                "PM",
                "PM",
                "pw"
        );

        meetingRecordCommandService.deleteMeetingRecord(MEETING_ID, principal);

        assertThat(meetingRecordCommandRepository.findByMeetingIdAndIsDeletedFalse(MEETING_ID)).isEmpty();
    }
}
