package com.moirai.alloc.calendar.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moirai.alloc.calendar.command.dto.request.*;
import com.moirai.alloc.calendar.command.dto.response.EventDetailResponse;
import com.moirai.alloc.calendar.command.dto.response.EventMemberResponse;
import com.moirai.alloc.calendar.command.dto.response.EventResponse;
import com.moirai.alloc.calendar.command.domain.entity.EventState;
import com.moirai.alloc.calendar.command.domain.entity.EventType;
import com.moirai.alloc.calendar.command.service.CalendarService;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.exception.GlobalExceptionHandler;
import com.moirai.alloc.common.exception.NotFoundException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = CalendarEventController.class)
@Import({
        GlobalExceptionHandler.class,
        CalendarEventControllerWebMvcTest.TestSecurityConfig.class,
        CalendarEventControllerWebMvcTest.ValidationConfig.class,
        CalendarEventControllerWebMvcTest.SecurityExceptionAdvice.class
})
class CalendarEventControllerWebMvcTest {

    private static final Long PROJECT_ID = 1L;
    private static final Long EVENT_ID = 100L;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private CalendarService calendarService;


    // =========================================================
    // Test Configs
    // =========================================================

    /**
     * 1) @PreAuthorize 동작을 위해 Method Security를 명시적으로 활성화
     * 2) REST 테스트에서 formLogin 리다이렉트 방지 + 401 보장을 위해 httpBasic 사용
     */
    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable()) // REST API 테스트 안정성(원하면 csrf()로 바꾸고 enable 가능)
                    .formLogin(form -> form.disable())
                    .httpBasic(basic -> { })
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }

    /** @Valid 검증이 slice 환경에서도 확실히 동작하도록 Validator를 명시 */
    @TestConfiguration
    static class ValidationConfig {
        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RestControllerAdvice
    static class SecurityExceptionAdvice {
        @ExceptionHandler(AccessDeniedException.class)
        public org.springframework.http.ResponseEntity<Void> handleAccessDenied(AccessDeniedException ex) {
            return org.springframework.http.ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // =========================================================
    // Helper: Authentication with UserPrincipal
    // =========================================================

    private Authentication authWithRole(String role, long userId) {
        UserPrincipal principal = Mockito.mock(UserPrincipal.class);
        when(principal.userId()).thenReturn(userId);
        when(principal.role()).thenReturn(role);

        // hasRole('PM') expects "ROLE_PM"
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    // =========================================================
    // Tests
    // =========================================================

    @Test
    @DisplayName("미인증 요청 -> 401 (Security 레벨 처리, ApiResponse 보장 아님)")
    void unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/calendar/events/{eventId}", PROJECT_ID, EVENT_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /shared: 성공(PM) -> 200 + ApiResponse.success 포맷 + Service 호출")
    void createSharedEvent_success_pm() throws Exception {
        EventResponse serviceRes = EventResponse.builder()
                .eventId(999L)
                .projectId(PROJECT_ID)
                .ownerUserId(10L)
                .eventName("공유 회의")
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDateTime(LocalDateTime.of(2026, 1, 10, 10, 0))
                .endDateTime(LocalDateTime.of(2026, 1, 10, 11, 0))
                .place("회의실 A")
                .description("주간 회의")
                .build();

        when(calendarService.createSharedEvent(eq(PROJECT_ID), any(SharedEventCreateRequest.class), any(UserPrincipal.class)))
                .thenReturn(serviceRes);

        String body = """
                {
                  "eventName": "공유 회의",
                  "startDateTime": "2026-01-10T10:00:00",
                  "endDateTime": "2026-01-10T11:00:00",
                  "place": "회의실 A",
                  "description": "주간 회의",
                  "memberUserIds": [1,2]
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/calendar/events/shared", PROJECT_ID)
                        .with(authentication(authWithRole("PM", 10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eventId").value(999))
                .andExpect(jsonPath("$.data.eventType").value("PUBLIC"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(calendarService, times(1))
                .createSharedEvent(eq(PROJECT_ID), any(SharedEventCreateRequest.class), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("POST /shared: 실패(USER Role) -> 403(PreAuthorize 차단) + Service 미호출")
    void createSharedEvent_forbidden_userRole() throws Exception {
        String body = """
                {
                  "eventName": "공유 회의",
                  "startDateTime": "2026-01-10T10:00:00",
                  "endDateTime": "2026-01-10T11:00:00",
                  "memberUserIds": [1]
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/calendar/events/shared", PROJECT_ID)
                        .with(authentication(authWithRole("USER", 20L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(calendarService, never())
                .createSharedEvent(anyLong(), any(SharedEventCreateRequest.class), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("POST /shared: Validation 실패(memberUserIds 누락) -> 400 + VALIDATION_ERROR/'Validation failed' + Service 미호출")
    void createSharedEvent_validation_fail_missingMemberUserIds() throws Exception {
        String body = """
                {
                  "eventName": "공유 회의",
                  "startDateTime": "2026-01-10T10:00:00",
                  "endDateTime": "2026-01-10T11:00:00"
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/calendar/events/shared", PROJECT_ID)
                        .with(authentication(authWithRole("PM", 10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(calendarService, never())
                .createSharedEvent(anyLong(), any(SharedEventCreateRequest.class), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("POST /personal: Validation 실패(eventName 누락) -> 400 + VALIDATION_ERROR/'Validation failed' + Service 미호출")
    void createPersonal_validation_fail_missingEventName() throws Exception {
        String body = """
                {
                  "startDateTime": "2026-01-10T09:00:00",
                  "endDateTime": "2026-01-10T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/calendar/events/personal", PROJECT_ID)
                        .with(authentication(authWithRole("USER", 20L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(calendarService, never())
                .createPersonalEvent(anyLong(), any(PersonalEventCreateRequest.class), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("POST /personal: Service IllegalArgumentException -> 400 + VALIDATION_ERROR/예외메시지")
    void createPersonal_service_illegalArgument() throws Exception {
        when(calendarService.createPersonalEvent(eq(PROJECT_ID), any(PersonalEventCreateRequest.class), any(UserPrincipal.class)))
                .thenThrow(new IllegalArgumentException("기간이 잘못되었습니다."));

        String body = """
                {
                  "eventName": "개인 일정",
                  "startDateTime": "2026-01-10T10:00:00",
                  "endDateTime": "2026-01-10T09:00:00"
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/calendar/events/personal", PROJECT_ID)
                        .with(authentication(authWithRole("USER", 20L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("기간이 잘못되었습니다."));

        verify(calendarService, times(1))
                .createPersonalEvent(eq(PROJECT_ID), any(PersonalEventCreateRequest.class), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("POST /vacation: Validation 실패(startDateTime 누락) -> 400 + VALIDATION_ERROR/'Validation failed' + Service 미호출")
    void createVacation_validation_fail_missingStart() throws Exception {
        String body = """
                {
                  "eventName": "휴가",
                  "endDateTime": "2026-01-11T23:59:00",
                  "description": "연차"
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/calendar/events/vacation", PROJECT_ID)
                        .with(authentication(authWithRole("USER", 20L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(calendarService, never())
                .createVacationEvent(anyLong(), any(VacationEventCreateRequest.class), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("PATCH /{eventId}/completion: Validation 실패(completed 누락) -> 400 + VALIDATION_ERROR/'Validation failed' + Service 미호출")
    void updateCompletion_validation_fail_missingCompleted() throws Exception {
        String body = """
                { }
                """;

        mockMvc.perform(patch("/api/projects/{projectId}/calendar/events/{eventId}/completion", PROJECT_ID, EVENT_ID)
                        .with(authentication(authWithRole("USER", 20L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(calendarService, never())
                .updateCompletion(anyLong(), anyLong(), any(EventCompletionRequest.class), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("PATCH /{eventId}/completion: 성공 -> 200 + ApiResponse.success + Service 호출")
    void updateCompletion_success() throws Exception {
        EventResponse serviceRes = EventResponse.builder()
                .eventId(EVENT_ID)
                .projectId(PROJECT_ID)
                .ownerUserId(20L)
                .eventName("개인 일정")
                .eventType(EventType.PRIVATE)
                .eventState(EventState.SUCCESS)
                .startDateTime(LocalDateTime.of(2026, 1, 10, 9, 0))
                .endDateTime(LocalDateTime.of(2026, 1, 10, 10, 0))
                .description("")
                .build();

        when(calendarService.updateCompletion(eq(PROJECT_ID), eq(EVENT_ID), any(EventCompletionRequest.class), any(UserPrincipal.class)))
                .thenReturn(serviceRes);

        String body = """
                { "completed": true }
                """;

        mockMvc.perform(patch("/api/projects/{projectId}/calendar/events/{eventId}/completion", PROJECT_ID, EVENT_ID)
                        .with(authentication(authWithRole("USER", 20L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eventId").value(100))
                .andExpect(jsonPath("$.data.eventState").value("SUCCESS"));

        verify(calendarService, times(1))
                .updateCompletion(eq(PROJECT_ID), eq(EVENT_ID), any(EventCompletionRequest.class), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("PATCH /{eventId}: Service ForbiddenException -> 403 + ApiResponse.failure(비어있지 않은 errorCode, message=예외메시지)")
    void updateEvent_service_forbidden() throws Exception {
        when(calendarService.updateEvent(eq(PROJECT_ID), eq(EVENT_ID), any(EventUpdateRequest.class), any(UserPrincipal.class)))
                .thenThrow(new ForbiddenException("권한이 없습니다."));

        String body = """
                { "eventName": "수정" }
                """;

        mockMvc.perform(patch("/api/projects/{projectId}/calendar/events/{eventId}", PROJECT_ID, EVENT_ID)
                        .with(authentication(authWithRole("USER", 20L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(Matchers.not(Matchers.isEmptyOrNullString())))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));

        verify(calendarService, times(1))
                .updateEvent(eq(PROJECT_ID), eq(EVENT_ID), any(EventUpdateRequest.class), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("DELETE /{eventId}: 성공 -> 200 + ApiResponse.success(data=null) + Service 호출")
    void deleteEvent_success() throws Exception {
        doNothing().when(calendarService).deleteEvent(eq(PROJECT_ID), eq(EVENT_ID), any(UserPrincipal.class));

        mockMvc.perform(delete("/api/projects/{projectId}/calendar/events/{eventId}", PROJECT_ID, EVENT_ID)
                        .with(authentication(authWithRole("USER", 20L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(calendarService, times(1))
                .deleteEvent(eq(PROJECT_ID), eq(EVENT_ID), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("GET /{eventId}: 성공 -> 200 + ApiResponse.success(data) 포맷")
    void getEventDetail_success() throws Exception {
        EventDetailResponse serviceRes = EventDetailResponse.builder()
                .eventId(EVENT_ID)
                .projectId(PROJECT_ID)
                .ownerUserId(10L)
                .eventName("공유 회의")
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDateTime(LocalDateTime.of(2026, 1, 10, 10, 0))
                .endDateTime(LocalDateTime.of(2026, 1, 10, 11, 0))
                .place("회의실 A")
                .description("주간 회의")
                .memberUserIds(List.of(1L, 2L))
                .members(List.of(
                        EventMemberResponse.of(1L, "김철수"),
                        EventMemberResponse.of(2L, "이영희")
                ))
                .build();

        when(calendarService.getEventDetail(eq(PROJECT_ID), eq(EVENT_ID), any(UserPrincipal.class)))
                .thenReturn(serviceRes);

        mockMvc.perform(get("/api/projects/{projectId}/calendar/events/{eventId}", PROJECT_ID, EVENT_ID)
                        .with(authentication(authWithRole("USER", 20L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eventId").value(100))
                .andExpect(jsonPath("$.data.eventType").value("PUBLIC"))
                .andExpect(jsonPath("$.data.memberUserIds[0]").value(1))
                .andExpect(jsonPath("$.data.members[0].userName").value("김철수"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(calendarService, times(1))
                .getEventDetail(eq(PROJECT_ID), eq(EVENT_ID), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("GET /{eventId}: Service NotFoundException -> 404 + ApiResponse.failure(비어있지 않은 errorCode, message=예외메시지)")
    void getEventDetail_notFound() throws Exception {
        when(calendarService.getEventDetail(eq(PROJECT_ID), eq(EVENT_ID), any(UserPrincipal.class)))
                .thenThrow(new NotFoundException("일정을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/projects/{projectId}/calendar/events/{eventId}", PROJECT_ID, EVENT_ID)
                        .with(authentication(authWithRole("USER", 20L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(Matchers.not(Matchers.isEmptyOrNullString())))
                .andExpect(jsonPath("$.message").value("일정을 찾을 수 없습니다."));

        verify(calendarService, times(1))
                .getEventDetail(eq(PROJECT_ID), eq(EVENT_ID), any(UserPrincipal.class));
    }
}
