package com.moirai.alloc.meeting.command.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.meeting.command.domain.command.domain.entity.Agenda;
import com.moirai.alloc.meeting.command.domain.command.domain.entity.MeetingRecord;
import com.moirai.alloc.meeting.command.domain.command.domain.entity.MeetingRecordLog;
import com.moirai.alloc.meeting.command.domain.command.domain.entity.Participant;
import com.moirai.alloc.meeting.command.dto.request.AgendaRequest;
import com.moirai.alloc.meeting.command.dto.request.CreateMeetingRecordRequest;
import com.moirai.alloc.meeting.command.dto.request.ParticipantRequest;
import com.moirai.alloc.meeting.command.dto.request.UpdateMeetingRecordRequest;
import com.moirai.alloc.meeting.command.repository.AgendaCommandRepository;
import com.moirai.alloc.meeting.command.repository.MeetingRecordCommandRepository;
import com.moirai.alloc.meeting.command.repository.MeetingRecordLogRepository;
import com.moirai.alloc.meeting.command.repository.ParticipantCommandRepository;
import com.moirai.alloc.meeting.query.repository.MeetingMembershipRepository;
import com.moirai.alloc.user.command.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MeetingRecordCommandService {

    private final MeetingRecordCommandRepository meetingRecordCommandRepository;
    private final AgendaCommandRepository agendaCommandRepository;
    private final ParticipantCommandRepository participantCommandRepository;
    private final MeetingMembershipRepository membershipRepository;
    private final MeetingRecordLogRepository meetingRecordLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MeetingRecordCommandService(MeetingRecordCommandRepository meetingRecordCommandRepository,
                                       AgendaCommandRepository agendaCommandRepository,
                                       ParticipantCommandRepository participantCommandRepository,
                                       MeetingMembershipRepository membershipRepository,
                                       MeetingRecordLogRepository meetingRecordLogRepository) {
        this.meetingRecordCommandRepository = meetingRecordCommandRepository;
        this.agendaCommandRepository = agendaCommandRepository;
        this.participantCommandRepository = participantCommandRepository;
        this.membershipRepository = membershipRepository;
        this.meetingRecordLogRepository = meetingRecordLogRepository;
    }

    @Transactional
    public Long createMeetingRecord(Long projectId, CreateMeetingRecordRequest request, UserPrincipal principal) {
        if (request.projectId() == null || !request.projectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로젝트 정보가 일치하지 않습니다.");
        }
        validateMembership(projectId, principal.userId());
        MeetingRecord meetingRecord = MeetingRecord.create(
                projectId,
                principal.loginId(),
                request.progress(),
                request.meetingDate(),
                request.meetingTime()
        );
        MeetingRecord saved = meetingRecordCommandRepository.save(meetingRecord);
        saveAgendas(saved, request.agendas());
        saveParticipants(saved, request.participants());
        logMeetingRecordChange(saved, principal.userId(), MeetingRecordLog.ActionType.CREATE,
                buildMeetingCreateMessage(saved));
        return saved.getMeetingId();
    }

    @Transactional
    public void updateMeetingRecord(Long projectId, UpdateMeetingRecordRequest request, UserPrincipal principal) {
        MeetingRecord meetingRecord = findMeetingRecord(request.meetingId());
        if (!meetingRecord.getProjectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회의록을 찾을 수 없습니다.");
        }
        validateMembership(meetingRecord.getProjectId(), principal.userId());
        validateOwnerOrPm(meetingRecord, principal);

        String updateMessage = buildMeetingUpdateMessage(meetingRecord, request);

        meetingRecord.updateMeetingInfo(
                request.progress(),
                request.meetingDate(),
                request.meetingTime()
        );

        if (request.agendas() != null) {
            agendaCommandRepository.deleteByMeetingMeetingId(meetingRecord.getMeetingId());
            saveAgendas(meetingRecord, request.agendas());
        }
        if (request.participants() != null) {
            participantCommandRepository.deleteByMeetingMeetingId(meetingRecord.getMeetingId());
            saveParticipants(meetingRecord, request.participants());
        }

        logMeetingRecordChange(meetingRecord, principal.userId(), MeetingRecordLog.ActionType.UPDATE, updateMessage);
    }

    @Transactional
    public void deleteMeetingRecord(Long projectId, Long meetingId, UserPrincipal principal) {
        MeetingRecord meetingRecord = findMeetingRecord(meetingId);
        if (!meetingRecord.getProjectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회의록을 찾을 수 없습니다.");
        }
        validateMembership(meetingRecord.getProjectId(), principal.userId());
        validateOwnerOrPm(meetingRecord, principal);
        meetingRecord.markDeleted();
        logMeetingRecordChange(meetingRecord, principal.userId(), MeetingRecordLog.ActionType.DELETE, "회의록 삭제");
    }

    private void logMeetingRecordChange(MeetingRecord record, Long actorUserId,
                                        MeetingRecordLog.ActionType actionType, String message) {
        MeetingRecordLog log = MeetingRecordLog.builder()
                .projectId(record.getProjectId())
                .meetingId(record.getMeetingId())
                .actorUserId(actorUserId)
                .actionType(actionType)
                .logMessage(message)
                .build();
        meetingRecordLogRepository.save(log);
    }

    private String buildMeetingCreateMessage(MeetingRecord record) {
        String datePart = formatMeetingDateTime(record.getMeetingDate(), record.getMeetingTime());
        String progressPart = (record.getProgress() != null) ? ("진행률 " + record.getProgress()) : null;
        List<String> parts = new ArrayList<>();
        if (datePart != null) parts.add(datePart);
        if (progressPart != null) parts.add(progressPart);
        if (parts.isEmpty()) {
            return "회의록 생성";
        }
        return "회의록 생성: " + String.join(", ", parts);
    }

    private String buildMeetingUpdateMessage(MeetingRecord record, UpdateMeetingRecordRequest request) {
        List<String> parts = new ArrayList<>();

        if (request.progress() != null && !request.progress().equals(record.getProgress())) {
            String before = record.getProgress() == null ? "-" : record.getProgress().toString();
            parts.add("진행률 " + before + " → " + request.progress());
        }

        if (request.meetingDate() != null && !request.meetingDate().equals(record.getMeetingDate())) {
            parts.add("회의일 변경");
        }
        if (request.meetingTime() != null && !request.meetingTime().equals(record.getMeetingTime())) {
            parts.add("회의시간 변경");
        }

        if (request.agendas() != null) {
            long before = agendaCommandRepository.countByMeetingMeetingId(record.getMeetingId());
            parts.add("안건 " + before + " → " + request.agendas().size());
        }
        if (request.participants() != null) {
            long before = participantCommandRepository.countByMeetingMeetingId(record.getMeetingId());
            parts.add("참석자 " + before + " → " + request.participants().size());
        }

        if (parts.isEmpty()) {
            return "회의록 수정";
        }
        return "회의록 수정: " + String.join(", ", parts);
    }

    private String formatMeetingDateTime(java.time.LocalDateTime meetingDate, java.time.LocalDateTime meetingTime) {
        if (meetingDate == null && meetingTime == null) return null;
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        String datePart = meetingDate == null ? "-" : meetingDate.format(dateFmt);
        String timePart = meetingTime == null ? "-" : meetingTime.format(timeFmt);
        return "일시 " + datePart + " " + timePart;
    }

    private MeetingRecord findMeetingRecord(Long meetingId) {
        return meetingRecordCommandRepository.findByMeetingIdAndIsDeletedFalse(meetingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회의록을 찾을 수 없습니다."));
    }

    private void validateMembership(Long projectId, Long userId) {
        if (!membershipRepository.existsMembership(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "프로젝트 멤버가 아닙니다.");
        }
    }

    private void validateOwnerOrPm(MeetingRecord meetingRecord, UserPrincipal principal) {
        boolean isPm = "PM".equalsIgnoreCase(principal.role());
        boolean isOwner = meetingRecord.getCreatedBy() != null
                && meetingRecord.getCreatedBy().equals(principal.loginId());
        if (!isPm && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }
    }

    private void saveAgendas(MeetingRecord meetingRecord, List<AgendaRequest> agendas) {
        if (agendas == null || agendas.isEmpty()) {
            return;
        }
        List<Agenda> entities = agendas.stream()
                .map(agenda -> Agenda.create(
                        meetingRecord,
                        agenda.discussionTitle(),
                        agenda.discussionContent(),
                        agenda.discussionResult(),
                        agenda.agendaType()
                ))
                .toList();
        agendaCommandRepository.saveAll(entities);
    }

    private void saveParticipants(MeetingRecord meetingRecord, List<ParticipantRequest> participants) {
        if (participants == null || participants.isEmpty()) {
            return;
        }
        List<Participant> entities = participants.stream()
                .map(participant -> Participant.create(
                        meetingRecord,
                        entityManager.getReference(User.class, participant.userId()),
                        participant.isHost()
                ))
                .toList();
        participantCommandRepository.saveAll(entities);
    }
}
