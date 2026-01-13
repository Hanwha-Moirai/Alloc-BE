package com.moirai.alloc.calendar.query.service;

import com.moirai.alloc.calendar.command.domain.entity.Events;
import com.moirai.alloc.calendar.command.dto.response.CalendarItemResponse;
import com.moirai.alloc.calendar.command.dto.response.CalendarItemType;
import com.moirai.alloc.calendar.command.dto.response.CalendarViewResponse;
import com.moirai.alloc.calendar.command.repository.EventsRepository;
import com.moirai.alloc.calendar.query.dto.MilestoneCalendarRow;
import com.moirai.alloc.calendar.query.dto.TaskCalendarRow;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.gantt.command.domain.repo.MilestoneRepository;
import com.moirai.alloc.gantt.command.domain.repo.TaskRepository;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarQueryServiceImpl implements CalendarQueryService {

    private final EventsRepository eventsRepository;
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    private final SquadAssignmentRepository squadAssignmentRepository;

    /**
     * 캘린더 통합 뷰 조회
     * - from/to 범위를 LocalDateTime으로 확장(from=00:00, to는 +1일 00:00 exclusive)
     * - Events: findVisibleEvents로 권한 필터링(PRIVATE는 본인만)
     * - Task/Milestone: 전용 Query로 프로젝트 범위 데이터 조회
     * - 모든 아이템을 CalendarItemResponse로 통일한 후 시작일시 기준 정렬
     */
    @Override
    @Transactional(readOnly = true)
    public CalendarViewResponse getCalendarView(Long projectId, LocalDate from, LocalDate to, String view, UserPrincipal principal) {

        checkProjectMembership(projectId, principal);
        validateDateRange(from, to);

        LocalDateTime fromStart = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();

        List<CalendarItemResponse> items = new ArrayList<>();

        // 이벤트(Events): 권한 기반 노출 필터링 포함
        List<Events> events = eventsRepository.findVisibleEvents(projectId, fromStart, toExclusive, principal.userId());
        for (Events event : events) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("eventType", event.getEventType());
            meta.put("eventState", event.getEventState());

            items.add(CalendarItemResponse.builder()
                    .itemType(CalendarItemType.EVENT)
                    .id(event.getId())
                    .title(event.getEventName())
                    .start(event.getStartDate())
                    .end(event.getEndDate())
                    .colorHint(event.getEventType().name())
                    .meta(meta)
                    .build());
        }

        // 태스크(Task): 마일스톤 조인 기반으로 project 범위 제한된 캘린더 row 조회
        List<TaskCalendarRow> tasks = taskRepository.findCalendarTasks(projectId, from, to);
        for (TaskCalendarRow t : tasks) {
            LocalDateTime start = t.startDate().atStartOfDay();
            LocalDateTime endExclusive = t.endDate().plusDays(1).atStartOfDay();

            boolean isMine = Objects.equals(t.assigneeUserId(), principal.userId());

            Map<String, Object> meta = new HashMap<>();
            meta.put("taskStatus", t.taskStatus());
            meta.put("taskCategory", t.taskCategory());
            meta.put("assigneeUserId", t.assigneeUserId());
            meta.put("milestoneId", t.milestoneId());
            meta.put("isMine", isMine);
            meta.put("taskScope", isMine ? "MY_TASK" : "TEAM_TASK");

            items.add(CalendarItemResponse.builder()
                    .itemType(CalendarItemType.TASK)
                    .id(t.taskId())
                    .title(t.taskName())
                    .start(start)
                    .end(endExclusive)
                    .colorHint("TASK")
                    .meta(meta)
                    .build());
        }

        // 마일스톤(Milestone): 프로젝트 범위 조회 후 캘린더 아이템 변환
        List<MilestoneCalendarRow> milestones = milestoneRepository.findCalendarMilestones(projectId, from, to);
        for (MilestoneCalendarRow m : milestones) {
            LocalDateTime start = m.startDate().atStartOfDay();
            LocalDateTime endExclusive = m.endDate().plusDays(1).atStartOfDay();

            Map<String, Object> meta = new HashMap<>();
            meta.put("achievementRate", m.achievementRate());

            items.add(CalendarItemResponse.builder()
                    .itemType(CalendarItemType.MILESTONE)
                    .id(m.milestoneId())
                    .title(m.milestoneName())
                    .start(start)
                    .end(endExclusive)
                    .colorHint("MILESTONE")
                    .meta(meta)
                    .build());
        }

        // 정렬: 시작일시 기준으로 UI 렌더링/타임라인에 유리하게 정렬
        items.sort(Comparator.comparing(CalendarItemResponse::getStart));

        return CalendarViewResponse.builder()
                .items(items)
                .build();
    }

    /** 프로젝트 ASSIGNED 멤버인지 검증 (아니면 Forbidden) */
    private void checkProjectMembership(Long projectId, UserPrincipal principal) {
        boolean isMember = squadAssignmentRepository
                .existsByProjectIdAndUserIdAndFinalDecision(projectId, principal.userId(), FinalDecision.ASSIGNED);

        if (!isMember) throw new ForbiddenException("프로젝트 참여자가 아닙니다.");
    }

    /** 조회 기간(from/to) 유효성 검증: null 금지 + from <= to */
    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("조회 시작일(from)과 종료일(to)은 필수입니다.");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작일(from)은 종료일(to)보다 이후일 수 없습니다.");
        }
    }
}
