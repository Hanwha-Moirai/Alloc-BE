package com.moirai.alloc.meeting.command.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.meeting.command.domain.command.domain.entity.Agenda;
import com.moirai.alloc.meeting.command.domain.command.domain.entity.MeetingRecord;
import com.moirai.alloc.meeting.command.domain.command.domain.entity.Participant;
import com.moirai.alloc.meeting.command.dto.request.AgendaRequest;
import com.moirai.alloc.meeting.command.dto.request.CreateMeetingRecordRequest;
import com.moirai.alloc.meeting.command.dto.request.ParticipantRequest;
import com.moirai.alloc.meeting.command.dto.request.UpdateMeetingRecordRequest;
import com.moirai.alloc.meeting.command.repository.AgendaCommandRepository;
import com.moirai.alloc.meeting.command.repository.MeetingRecordCommandRepository;
import com.moirai.alloc.meeting.command.repository.ParticipantCommandRepository;
import com.moirai.alloc.meeting.query.repository.MeetingMembershipRepository;
import com.moirai.alloc.user.command.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MeetingRecordCommandService {

    private final MeetingRecordCommandRepository meetingRecordCommandRepository;
    private final AgendaCommandRepository agendaCommandRepository;
    private final ParticipantCommandRepository participantCommandRepository;
    private final MeetingMembershipRepository membershipRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MeetingRecordCommandService(MeetingRecordCommandRepository meetingRecordCommandRepository,
                                       AgendaCommandRepository agendaCommandRepository,
                                       ParticipantCommandRepository participantCommandRepository,
                                       MeetingMembershipRepository membershipRepository) {
        this.meetingRecordCommandRepository = meetingRecordCommandRepository;
        this.agendaCommandRepository = agendaCommandRepository;
        this.participantCommandRepository = participantCommandRepository;
        this.membershipRepository = membershipRepository;
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
