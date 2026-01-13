package com.moirai.alloc.calendar.command.service;

import com.moirai.alloc.calendar.command.domain.entity.*;
import com.moirai.alloc.calendar.command.dto.request.*;
import com.moirai.alloc.calendar.command.dto.response.EventDetailResponse;
import com.moirai.alloc.calendar.command.dto.response.EventMemberResponse;
import com.moirai.alloc.calendar.command.dto.response.EventResponse;
import com.moirai.alloc.calendar.command.repository.EventsLogRepository;
import com.moirai.alloc.calendar.command.repository.EventsRepository;
import com.moirai.alloc.calendar.command.repository.PublicEventsMemberRepository;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.exception.NotFoundException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final EventsRepository eventsRepository;
    private final PublicEventsMemberRepository publicEventsMemberRepository;
    private final EventsLogRepository eventsLogRepository;
    private final SquadAssignmentRepository squadAssignmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public EventResponse createSharedEvent(Long projectId, SharedEventCreateRequest request, UserPrincipal principal) {
        validatePeriod(request.getStartDateTime(), request.getEndDateTime());
        checkProjectMembership(projectId, principal);

        if (!isPm(principal)) throw new ForbiddenException("공유 일정은 PM만 생성할 수 있습니다.");

        validatePublicMembersRequired(request.getMemberUserIds());

        // 참여자 프로젝트 멤버(ASSIGNED) 검증 + 중복 제거
        List<Long> distinctMemberIds = distinct(request.getMemberUserIds());
        validateMembersBelongToProject(projectId, distinctMemberIds);

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

        List<PublicEventsMember> members = distinctMemberIds.stream()
                .map(userId -> PublicEventsMember.builder()
                        .eventId(saved.getId())
                        .userId(userId)
                        .build())
                .toList();
        publicEventsMemberRepository.saveAll(members);

        logChange(saved.getId(), principal.userId(), ChangeType.CREATE, "공유 일정 생성",
                null, saved.getStartDate(), null, saved.getEndDate());

        return EventResponse.from(saved); // 상세 조회는 GET /{eventId}로 memberUserIds 제공
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

        logChange(saved.getId(), principal.userId(), ChangeType.CREATE, "개인 일정 생성",
                null, saved.getStartDate(), null, saved.getEndDate());

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

        logChange(saved.getId(), principal.userId(), ChangeType.CREATE, "휴가 일정 생성",
                null, saved.getStartDate(), null, saved.getEndDate());

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

        logChange(event.getId(), principal.userId(), ChangeType.UPDATE,
                "완료 상태 변경: " + nextState,
                event.getStartDate(), event.getStartDate(),
                event.getEndDate(), event.getEndDate());

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

        // 기간 변경
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

        // PUBLIC로 변경되는 경우 memberUserIds가 반드시 필요 (빈 리스트 금지)
        boolean changedToPublic = (beforeType != EventType.PUBLIC && afterType == EventType.PUBLIC);
        if (changedToPublic && request.getMemberUserIds() == null) {
            throw new IllegalArgumentException("PUBLIC(공유 일정)으로 변경 시 참여자 목록(memberUserIds)은 필수입니다.");
        }

        // 멤버 변경
        if (request.getMemberUserIds() != null) {
            if (afterType != EventType.PUBLIC) {
                throw new ForbiddenException("공유 일정(PUBLIC)에서만 구성원(memberUserIds)을 변경할 수 있습니다.");
            }
            if (!isPm(principal) && !Objects.equals(event.getOwnerUserId(), principal.userId())) {
                throw new ForbiddenException("공유 일정의 구성원은 PM 또는 일정 작성자만 변경할 수 있습니다.");
            }

            validatePublicMembersRequired(request.getMemberUserIds());

            // 참여자 프로젝트 멤버(ASSIGNED) 검증 + distinct
            List<Long> distinctMemberIds = distinct(request.getMemberUserIds());
            validateMembersBelongToProject(projectId, distinctMemberIds);

            publicEventsMemberRepository.deleteByEventId(event.getId());

            List<PublicEventsMember> members = distinctMemberIds.stream()
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

        logChange(event.getId(), principal.userId(), ChangeType.UPDATE,
                "일정 수정",
                beforeStart, event.getStartDate(),
                beforeEnd, event.getEndDate());

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

        logChange(event.getId(), principal.userId(), ChangeType.DELETE, "일정 삭제",
                beforeStart, null, beforeEnd, null);
    }

    /** 이벤트 단건 조회 + PUBLIC이면 참여자 이름 포함 */
    @Override
    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long projectId, Long eventId, UserPrincipal principal) {
        checkProjectMembership(projectId, principal);

        Events event = eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId)
                .orElseThrow(() -> new NotFoundException("일정을 찾을 수 없습니다."));

        Long requesterId = principal.userId();
        boolean isOwner = Objects.equals(event.getOwnerUserId(), requesterId);
        boolean pm = isPm(principal);

        // PRIVATE는 작성자만 조회 가능
        if (event.getEventType() == EventType.PRIVATE && !isOwner) {
            throw new ForbiddenException("개인 일정(PRIVATE)은 작성자만 조회할 수 있습니다.");
        }

        List<Long> memberUserIds = List.of();
        List<EventMemberResponse> members = List.of();

        if (event.getEventType() == EventType.PUBLIC) {
            // 참여자 ID (중복 제거 + 순서 보존)
            List<Long> orderedDistinctIds = publicEventsMemberRepository.findByEventId(event.getId()).stream()
                    .map(PublicEventsMember::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(LinkedHashSet::new),
                            ArrayList::new
                    ));

            memberUserIds = orderedDistinctIds;

            // user 테이블에서 이름 조회
            List<User> userEntities = orderedDistinctIds.isEmpty()
                    ? List.of()
                    : userRepository.findByIdIn(orderedDistinctIds);

            Map<Long, String> nameById = userEntities.stream()
                    .collect(Collectors.toMap(User::getUserId, User::getUserName, (a, b) -> a));

            members = orderedDistinctIds.stream()
                    .map(id -> EventMemberResponse.of(id, nameById.getOrDefault(id, "")))
                    .toList();
        }

        return EventDetailResponse.from(event, memberUserIds, members);
    }

    private void checkProjectMembership(Long projectId, UserPrincipal principal) {
        boolean isMember = squadAssignmentRepository
                .existsByProjectIdAndUserIdAndFinalDecision(projectId, principal.userId(), FinalDecision.ASSIGNED);
        if (!isMember) throw new ForbiddenException("프로젝트 참여자가 아닙니다.");
    }

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
        if (!isOwner && !pm) throw new ForbiddenException("공유 일정(PUBLIC)은 작성자 또는 PM만 수정/삭제할 수 있습니다.");
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

    private void logChange(Long eventId, Long actorUserId, ChangeType changeType,
                           String description, LocalDateTime beforeStart, LocalDateTime afterStart,
                           LocalDateTime beforeEnd, LocalDateTime afterEnd) {
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
        if (start == null || end == null) throw new IllegalArgumentException("시작 일시와 종료 일시는 필수입니다.");
        if (!start.isBefore(end)) throw new IllegalArgumentException("시작 일시는 종료 일시보다 이전이어야 합니다.");
    }

    private void validatePublicMembersRequired(List<Long> memberUserIds) {
        if (memberUserIds == null || memberUserIds.isEmpty())
            throw new IllegalArgumentException("공유 일정은 참여자 목록(memberUserIds)이 필수입니다.");
        if (memberUserIds.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("참여자 목록(memberUserIds)에는 null이 포함될 수 없습니다.");
    }

    private List<Long> distinct(List<Long> ids) {
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    /** memberUserIds 전원이 프로젝트 ASSIGNED 멤버인지 검증 */
    private void validateMembersBelongToProject(Long projectId, List<Long> memberUserIds) {
        if (memberUserIds == null || memberUserIds.isEmpty()) return;

        List<Long> distinct = memberUserIds.stream().distinct().toList();
        List<Long> found = squadAssignmentRepository.findUserIdsInProjectByDecision(
                projectId, FinalDecision.ASSIGNED, distinct
        );

        Set<Long> foundSet = new HashSet<>(found);
        List<Long> missing = distinct.stream()
                .filter(id -> !foundSet.contains(id))
                .toList();

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("프로젝트 참여자가 아닌 사용자(memberUserIds)가 포함되어 있습니다: " + missing);
        }
    }
}
