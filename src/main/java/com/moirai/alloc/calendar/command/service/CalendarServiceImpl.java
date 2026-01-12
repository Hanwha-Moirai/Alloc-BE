package com.moirai.alloc.calendar.command.service;

import com.moirai.alloc.calendar.command.domain.entity.*;
import com.moirai.alloc.calendar.command.dto.request.*;
import com.moirai.alloc.calendar.command.dto.response.EventResponse;
import com.moirai.alloc.calendar.command.repository.EventsLogRepository;
import com.moirai.alloc.calendar.command.repository.EventsRepository;
import com.moirai.alloc.calendar.command.repository.PublicEventsMemberRepository;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.exception.NotFoundException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.domain.FinalDecision;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final EventsRepository eventsRepository;
    private final PublicEventsMemberRepository publicEventsMemberRepository;
    private final EventsLogRepository eventsLogRepository;
    private final SquadAssignmentRepository squadAssignmentRepository;

    @Override
    @Transactional
    public EventResponse createSharedEvent(Long projectId, SharedEventCreateRequest request, UserPrincipal principal) {
        validatePeriod(request.getStartDateTime(), request.getEndDateTime());
        checkProjectMembership(projectId, principal);

        if (!isPm(principal)) {
            throw new ForbiddenException("공유 일정은 PM만 생성할 수 있습니다.");
        }

        // DTO에서 @NotEmpty + List<@NotNull Long>로 막지만, 서비스에서도 방어
        validatePublicMembersRequired(request.getMemberUserIds());

        Events event = Events.builder()
                .projectId(projectId)
                .ownerUserId(principal.userId())
                .eventName(request.getEventName())
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDate(request.getStartDateTime())
                .endDate(request.getEndDateTime())
                .eventPlace(request.getPlace())
                .eventDescription(defaultDescription(request.getDescription()))
                .build();

        Events saved = eventsRepository.save(event);

        List<PublicEventsMember> members = request.getMemberUserIds().stream()
                .map(userId -> PublicEventsMember.builder()
                        .eventId(saved.getId())
                        .userId(userId)
                        .build())
                .toList();

        publicEventsMemberRepository.saveAll(members);

        logChange(
                saved.getId(), principal.userId(), ChangeType.CREATE, "공유 일정 생성",
                null, saved.getStartDate(),
                null, saved.getEndDate()
        );

        return EventResponse.from(saved);
    }

    @Override
    @Transactional
    public EventResponse createPersonalEvent(Long projectId, PersonalEventCreateRequest request, UserPrincipal principal) {
        validatePeriod(request.getStartDateTime(), request.getEndDateTime());
        checkProjectMembership(projectId, principal);

        Events event = Events.builder()
                .projectId(projectId)
                .ownerUserId(principal.userId())
                .eventName(request.getEventName())
                .eventType(EventType.PRIVATE)
                .eventState(EventState.IN_PROGRESS)
                .startDate(request.getStartDateTime())
                .endDate(request.getEndDateTime())
                .eventPlace(request.getPlace())
                .eventDescription(defaultDescription(request.getDescription()))
                .build();

        Events saved = eventsRepository.save(event);

        logChange(
                saved.getId(), principal.userId(), ChangeType.CREATE, "개인 일정 생성",
                null, saved.getStartDate(),
                null, saved.getEndDate()
        );

        return EventResponse.from(saved);
    }

    @Override
    @Transactional
    public EventResponse createVacationEvent(Long projectId, VacationEventCreateRequest request, UserPrincipal principal) {
        validatePeriod(request.getStartDateTime(), request.getEndDateTime());
        checkProjectMembership(projectId, principal);

        String eventName = (request.getEventName() == null || request.getEventName().isBlank())
                ? "휴가"
                : request.getEventName();

        Events event = Events.builder()
                .projectId(projectId)
                .ownerUserId(principal.userId())
                .eventName(eventName)
                .eventType(EventType.VACATION)
                .eventState(EventState.IN_PROGRESS)
                .startDate(request.getStartDateTime())
                .endDate(request.getEndDateTime())
                .eventDescription(defaultDescription(request.getDescription()))
                .build();

        Events saved = eventsRepository.save(event);

        logChange(
                saved.getId(), principal.userId(), ChangeType.CREATE, "휴가 일정 생성",
                null, saved.getStartDate(),
                null, saved.getEndDate()
        );

        return EventResponse.from(saved);
    }

    @Override
    @Transactional
    public EventResponse updateCompletion(Long projectId, Long eventId, EventCompletionRequest request, UserPrincipal principal) {
        Events event = getEventOrThrow(projectId, eventId);
        checkEventPermission(projectId, event, principal);

        EventState nextState = Boolean.TRUE.equals(request.getCompleted())
                ? EventState.SUCCESS
                : EventState.IN_PROGRESS;

        event.updateEventState(nextState);

        logChange(
                event.getId(), principal.userId(), ChangeType.UPDATE,
                "완료 상태 변경: " + nextState,
                event.getStartDate(), event.getStartDate(),
                event.getEndDate(), event.getEndDate()
        );

        return EventResponse.from(event);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long projectId, Long eventId, EventUpdateRequest request, UserPrincipal principal) {
        Events event = getEventOrThrow(projectId, eventId);
        checkEventPermission(projectId, event, principal);

        LocalDateTime beforeStart = event.getStartDate();
        LocalDateTime beforeEnd = event.getEndDate();
        EventType beforeType = event.getEventType();

        // 이름/장소/설명
        if (request.getEventName() != null) event.updateEventName(request.getEventName());
        if (request.getPlace() != null) event.updateEventPlace(request.getPlace());
        if (request.getDescription() != null) event.updateEventDescription(request.getDescription());

        // 기간 변경 (+ 유효성)
        if (request.getStartDateTime() != null || request.getEndDateTime() != null) {
            LocalDateTime newStart = (request.getStartDateTime() != null) ? request.getStartDateTime() : event.getStartDate();
            LocalDateTime newEnd = (request.getEndDateTime() != null) ? request.getEndDateTime() : event.getEndDate();
            validatePeriod(newStart, newEnd);
            event.updatePeriod(newStart, newEnd);
        }

        // 타입 변경
        if (request.getEventType() != null) {
            if (event.getEventType() == EventType.PUBLIC
                    && request.getEventType() != EventType.PUBLIC
                    && !isPm(principal)) {
                throw new ForbiddenException("공유 일정(PUBLIC) 타입 변경은 PM만 가능합니다.");
            }
            if (request.getEventType() == EventType.PUBLIC && !isPm(principal)) {
                throw new ForbiddenException("공유 일정(PUBLIC)으로 변경은 PM만 가능합니다.");
            }
            event.updateEventType(request.getEventType());
        }

        EventType afterType = event.getEventType();

        // 멤버 변경: PUBLIC에서만 + (PM 또는 작성자)만 가능 + "구성원 0명 금지"
        if (request.getMemberUserIds() != null) {
            if (afterType != EventType.PUBLIC) {
                throw new ForbiddenException("공유 일정(PUBLIC)에서만 구성원(memberUserIds)을 변경할 수 있습니다.");
            }
            if (!isPm(principal) && !Objects.equals(event.getOwnerUserId(), principal.userId())) {
                throw new ForbiddenException("공유 일정의 구성원은 PM 또는 일정 작성자만 변경할 수 있습니다.");
            }

            // PUBLIC 일정에서 memberUserIds 빈 리스트로 수정 차단
            validatePublicMembersRequired(request.getMemberUserIds());

            publicEventsMemberRepository.deleteByEventId(event.getId());

            List<PublicEventsMember> members = request.getMemberUserIds().stream()
                    .map(userId -> PublicEventsMember.builder()
                            .eventId(event.getId())
                            .userId(userId)
                            .build())
                    .toList();

            publicEventsMemberRepository.saveAll(members);
        }

        // PUBLIC -> PRIVATE/VACATION 변경 시 오염 방지
        if (beforeType == EventType.PUBLIC && afterType != EventType.PUBLIC) {
            publicEventsMemberRepository.deleteByEventId(event.getId());
        }

        LocalDateTime afterStart = event.getStartDate();
        LocalDateTime afterEnd = event.getEndDate();

        logChange(
                event.getId(), principal.userId(), ChangeType.UPDATE,
                "일정 수정",
                beforeStart, afterStart,
                beforeEnd, afterEnd
        );

        return EventResponse.from(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long projectId, Long eventId, UserPrincipal principal) {
        Events event = getEventOrThrow(projectId, eventId);
        checkEventPermission(projectId, event, principal);

        LocalDateTime beforeStart = event.getStartDate();
        LocalDateTime beforeEnd = event.getEndDate();

        event.softDelete();
        publicEventsMemberRepository.deleteByEventId(event.getId());

        logChange(
                event.getId(), principal.userId(), ChangeType.DELETE, "일정 삭제",
                beforeStart, null,
                beforeEnd, null
        );
    }

    /**
     * 프로젝트 참여자(= 동료/PM 포함)인지 검증
     * - squad_assignment.final_decision = ASSIGNED 를 “프로젝트 참여”로 간주
     */
    private void checkProjectMembership(Long projectId, UserPrincipal principal) {
        boolean isMember = squadAssignmentRepository
                .existsByProjectIdAndUserIdAndFinalDecision(projectId, principal.userId(), FinalDecision.ASSIGNED);

        if (!isMember) {
            throw new ForbiddenException("프로젝트 참여자가 아닙니다.");
        }
    }

    /**
     * 이벤트 수정/삭제/완료처리 권한 검증
     * - PRIVATE: 본인(owner)만 가능 (PM도 불가)
     * - VACATION: 본인(owner) 또는 PM 가능
     * - PUBLIC: PM 또는 본인(owner) 가능
     */
    private void checkEventPermission(Long projectId, Events event, UserPrincipal principal) {
        checkProjectMembership(projectId, principal);

        Long requesterId = principal.userId();
        boolean isOwner = Objects.equals(event.getOwnerUserId(), requesterId);
        boolean pm = isPm(principal);

        if (event.getEventType() == EventType.PRIVATE) {
            if (!isOwner) throw new ForbiddenException("개인 일정(PRIVATE)은 작성자만 수정/삭제할 수 있습니다.");
            return;
        }

        if (event.getEventType() == EventType.VACATION) {
            if (!isOwner && !pm) throw new ForbiddenException("휴가 일정(VACATION)은 작성자 또는 PM만 수정/삭제할 수 있습니다.");
            return;
        }

        if (!isOwner && !pm) {
            throw new ForbiddenException("공유 일정(PUBLIC)은 작성자 또는 PM만 수정/삭제할 수 있습니다.");
        }
    }

    private boolean isPm(UserPrincipal principal) {
        String role = principal.role();
        if (role == null) return false;
        role = role.startsWith("ROLE_") ? role.substring(5) : role;
        return "PM".equalsIgnoreCase(role);
    }

    private Events getEventOrThrow(Long projectId, Long eventId) {
        return eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId)
                .orElseThrow(() -> new NotFoundException("일정을 찾을 수 없습니다."));
    }

    private void logChange(
            Long eventId,
            Long actorUserId,
            ChangeType changeType,
            String description,
            LocalDateTime beforeStart,
            LocalDateTime afterStart,
            LocalDateTime beforeEnd,
            LocalDateTime afterEnd
    ) {
        EventsLog log = EventsLog.builder()
                .eventId(eventId)
                .actorUserId(actorUserId)
                .changeType(changeType)
                .logDescription(description)
                .beforeStartDate(beforeStart)
                .afterStartDate(afterStart)
                .beforeEndDate(beforeEnd)
                .afterEndDate(afterEnd)
                .build();

        eventsLogRepository.save(log);
    }

    private String defaultDescription(String description) {
        return (description == null || description.isBlank()) ? "" : description;
    }

    private void validatePeriod(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("시작 일시와 종료 일시는 필수입니다.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("시작 일시는 종료 일시보다 이전이어야 합니다.");
        }
    }

    /**
     * PUBLIC 일정은 참여자 목록이 "필수"이며 "0명"을 허용하지 않음.
     */
    private void validatePublicMembersRequired(List<Long> memberUserIds) {
        if (memberUserIds == null || memberUserIds.isEmpty()) {
            throw new IllegalArgumentException("공유 일정은 참여자 목록(memberUserIds)이 필수입니다.");
        }
        if (memberUserIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("참여자 목록(memberUserIds)에는 null이 포함될 수 없습니다.");
        }
    }
}
