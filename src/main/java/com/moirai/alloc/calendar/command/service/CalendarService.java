package com.moirai.alloc.calendar.command.service;

import com.moirai.alloc.calendar.command.dto.request.*;
import com.moirai.alloc.calendar.command.dto.response.EventDetailResponse;
import com.moirai.alloc.calendar.command.dto.response.EventResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;

public interface CalendarService {

    /**
     * 공유 일정(PUBLIC) 생성 (PM 전용)
     * - 기간 검증(시작 < 종료), 프로젝트 멤버십 검증
     * - 참여자(memberUserIds) 필수 + 프로젝트 ASSIGNED 멤버인지 검증
     * - events 저장 + public_events_member 저장 + events_log 기록
     */
    EventResponse createSharedEvent(Long projectId, SharedEventCreateRequest request, UserPrincipal principal);

    /**
     * 개인 일정(PRIVATE) 생성 (PM/USER)
     * - 기간 검증, 프로젝트 멤버십 검증
     * - events 저장 + events_log 기록
     */
    EventResponse createPersonalEvent(Long projectId, PersonalEventCreateRequest request, UserPrincipal principal);

    /**
     * 휴가 일정(VACATION) 생성 (PM/USER)
     * - 기간 검증, 프로젝트 멤버십 검증
     * - eventName 미입력 시 기본값("휴가")
     * - events 저장 + events_log 기록
     */
    EventResponse createVacationEvent(Long projectId, VacationEventCreateRequest request, UserPrincipal principal);

    /**
     * 일정 완료/미완료 상태 변경 (PM/USER)
     * - 권한 검증(작성자/PM 등, 타입별 규칙)
     * - completed=true -> SUCCESS, false -> IN_PROGRESS
     * - events_log 기록
     */
    EventResponse updateCompletion(Long projectId, Long eventId, EventCompletionRequest request, UserPrincipal principal);

    /**
     * 일정 수정 (PM/USER)
     * - 부분 수정(널이면 미수정)
     * - 기간 변경 시 기간 검증
     * - 타입 변경 시 권한/규칙 검증(PUBLIC 관련)
     * - PUBLIC 참여자(memberUserIds) 변경/검증 및 PUBLIC->비PUBLIC 전환 시 참여자 매핑 정리
     * - events_log 기록
     */
    EventResponse updateEvent(Long projectId, Long eventId, EventUpdateRequest request, UserPrincipal principal);

    /**
     * 일정 삭제 (PM/USER)
     * - Soft delete(is_deleted=true)
     * - PUBLIC이면 public_events_member 정리
     * - events_log 기록
     */
    void deleteEvent(Long projectId, Long eventId, UserPrincipal principal);

    /**
     * 일정 상세 조회 (PM/USER)
     * - PRIVATE는 작성자만 조회 가능
     * - PUBLIC이면 참여자 ID + 참여자 이름 목록까지 포함하여 반환
     */
    EventDetailResponse getEventDetail(Long projectId, Long eventId, UserPrincipal principal);
}
