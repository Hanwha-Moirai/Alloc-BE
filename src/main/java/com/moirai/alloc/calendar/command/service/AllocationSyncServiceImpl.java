package com.moirai.alloc.calendar.command.service;

import com.moirai.alloc.calendar.command.domain.entity.*;
import com.moirai.alloc.calendar.command.dto.response.AllocationSyncResponse;
import com.moirai.alloc.calendar.command.repository.EventsLogRepository;
import com.moirai.alloc.calendar.command.repository.EventsRepository;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.exception.NotFoundException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.domain.FinalDecision;
import com.moirai.alloc.management.domain.SquadAssignment;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AllocationSyncServiceImpl implements AllocationSyncService {

    private static final String ALLOCATION_EVENT_NAME = "Allocation";
    private static final String ALLOCATION_EVENT_DESC = "인력 배정 정보와 동기화된 일정입니다.";

    private final SquadAssignmentRepository squadAssignmentRepository;
    private final EventsRepository eventsRepository;
    private final EventsLogRepository eventsLogRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public AllocationSyncResponse syncToCalendar(Long projectId, UserPrincipal principal) {

        // 프로젝트 참여자 검증 + (권장) PM 권한 방어
        checkProjectMembership(projectId, principal);
        if (!isPm(principal)) {
            throw new ForbiddenException("PM만 인력 배정 캘린더 동기화를 실행할 수 있습니다.");
        }

        // 배정 기간 산정: 프로젝트 기간을 기본으로 사용
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("프로젝트를 찾을 수 없습니다."));

        LocalDateTime projectStart = project.getStartDate().atStartOfDay();
        // 반개구간 [start, endExclusive)로 다루기 위해 end는 다음날 00:00
        LocalDateTime projectEndExclusive = project.getEndDate().plusDays(1).atStartOfDay();

        List<SquadAssignment> assignments = squadAssignmentRepository
                .findByProjectIdAndFinalDecision(projectId, FinalDecision.ASSIGNED);

        int upserted = 0;

        for (SquadAssignment assignment : assignments) {
            Long targetUserId = assignment.getUserId();

            // decidedAt이 있으면 시작을 decidedAt으로 (단, 프로젝트 시작 이전이면 프로젝트 시작으로 보정)
            LocalDateTime decidedAt = assignment.getDecidedAt();
            LocalDateTime start = (decidedAt != null) ? max(projectStart, decidedAt) : projectStart;
            LocalDateTime end = projectEndExclusive;

            // 시작/종료 역전 방어: decidedAt이 프로젝트 종료 이후면 해당 배정은 스킵
            if (!start.isBefore(end)) {
                continue;
            }

            // 멱등 Upsert: 기존 Allocation 이벤트가 있으면 UPDATE, 없으면 CREATE
            Events event = eventsRepository
                    .findByProjectIdAndOwnerUserIdAndEventTypeAndEventNameAndDeletedFalse(
                            projectId, targetUserId, EventType.PUBLIC, ALLOCATION_EVENT_NAME
                    )
                    .orElse(null);

            if (event == null) {
                // CREATE
                Events created = Events.builder()
                        .projectId(projectId)
                        .ownerUserId(targetUserId)
                        .eventName(ALLOCATION_EVENT_NAME)
                        .eventType(EventType.PUBLIC)
                        .eventState(EventState.IN_PROGRESS)
                        .startDate(start)
                        .endDate(end)
                        .eventDescription(ALLOCATION_EVENT_DESC)
                        .build();

                Events saved = eventsRepository.save(created);

                logChange(
                        saved.getId(), principal.userId(), ChangeType.CREATE,
                        "인력 배정 일정이 생성되었습니다.",
                        null, saved.getStartDate(),
                        null, saved.getEndDate()
                );

                upserted++;
            } else {
                // UPDATE (기간/설명만 동기화)
                LocalDateTime beforeStart = event.getStartDate();
                LocalDateTime beforeEnd = event.getEndDate();

                boolean changed = false;

                if (!Objects.equals(beforeStart, start) || !Objects.equals(beforeEnd, end)) {
                    event.updatePeriod(start, end);
                    changed = true;
                }

                if (!Objects.equals(event.getEventDescription(), ALLOCATION_EVENT_DESC)) {
                    event.updateEventDescription(ALLOCATION_EVENT_DESC);
                    changed = true;
                }

                if (changed) {
                    logChange(
                            event.getId(), principal.userId(), ChangeType.UPDATE,
                            "인력 배정 일정이 동기화(수정)되었습니다.",
                            beforeStart, event.getStartDate(),
                            beforeEnd, event.getEndDate()
                    );
                    upserted++;
                }
            }
        }

        return AllocationSyncResponse.builder()
                .processedUsers(assignments.size())
                .upsertedEvents(upserted)
                .build();
    }

    /**
     * 프로젝트 참여자(동료/PM 포함)인지 검증
     * - squad_assignment.final_decision = ASSIGNED 를 “프로젝트 참여”로 간주
     */
    private void checkProjectMembership(Long projectId, UserPrincipal principal) {
        boolean isMember = squadAssignmentRepository
                .existsByProjectIdAndUserIdAndFinalDecision(projectId, principal.userId(), FinalDecision.ASSIGNED);

        if (!isMember) {
            throw new ForbiddenException("프로젝트 참여자가 아닙니다.");
        }
    }

    private boolean isPm(UserPrincipal principal) {
        String role = principal.role();
        if (role == null) return false;
        role = role.startsWith("ROLE_") ? role.substring(5) : role;
        return "PM".equalsIgnoreCase(role);
    }

    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
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
}
