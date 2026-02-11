package com.moirai.alloc.calendar.query.controller;

import com.moirai.alloc.calendar.query.dto.TodayEventItemResponse;
import com.moirai.alloc.calendar.query.dto.TodayEventsResponse;
import com.moirai.alloc.calendar.query.dto.WeeklyEventCountResponse;
import com.moirai.alloc.calendar.query.service.CalendarQueryService;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.exception.GlobalExceptionHandler;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = CalendarSummaryController.class)
@Import({
        GlobalExceptionHandler.class,
        CalendarSummaryControllerWebMvcTest.TestSecurityConfig.class,
        CalendarSummaryControllerWebMvcTest.SecurityExceptionAdvice.class
})
class CalendarSummaryControllerWebMvcTest {

    @Autowired MockMvc mockMvc;

    @MockBean
    private CalendarQueryService calendarQueryService;

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .formLogin(form -> form.disable())
                    .httpBasic(basic -> { })
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
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

    private Authentication authWithRole(String role, long userId) {
        UserPrincipal principal = Mockito.mock(UserPrincipal.class);
        when(principal.userId()).thenReturn(userId);
        when(principal.role()).thenReturn(role);

        return new UsernamePasswordAuthenticationToken(
                principal,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    @Test
    @DisplayName("GET /api/calendar/weekly-count: 미인증 -> 401")
    void weeklyCount_unauthenticated_401() throws Exception {
        mockMvc.perform(get("/api/calendar/weekly-count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/calendar/weekly-count: 성공 -> 200 + ApiResponse.success 포맷")
    void weeklyCount_success() throws Exception {
        WeeklyEventCountResponse serviceRes = new WeeklyEventCountResponse(
                LocalDate.of(2026, 1, 19),
                LocalDate.of(2026, 1, 25),
                12L
        );

        when(calendarQueryService.getMyWeeklyEventCount(any(UserPrincipal.class)))
                .thenReturn(serviceRes);

        mockMvc.perform(get("/api/calendar/weekly-count")
                        .with(authentication(authWithRole("USER", 20L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.weekStart").value("2026-01-19"))
                .andExpect(jsonPath("$.data.weekEnd").value("2026-01-25"))
                .andExpect(jsonPath("$.data.total").value(12))
                .andExpect(jsonPath("$.data.count").value(12))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(calendarQueryService, times(1)).getMyWeeklyEventCount(any(UserPrincipal.class));
    }

    @Test
    @DisplayName("GET /api/calendar/today: 성공(default limit=50) -> 200 + 파라미터 전달 검증")
    void today_success_defaultLimit() throws Exception {
        TodayEventsResponse serviceRes = new TodayEventsResponse(
                LocalDate.of(2026, 1, 21),
                List.of(new TodayEventItemResponse(
                        100L, 1L, "A",
                        LocalDateTime.of(2026, 1, 21, 9, 0),
                        LocalDateTime.of(2026, 1, 21, 10, 0),
                        com.moirai.alloc.calendar.command.domain.entity.EventType.PUBLIC
                )),
                null,
                null
        );

        when(calendarQueryService.getMyTodayEvents(anyInt(), any(), any(), any(UserPrincipal.class)))
                .thenReturn(serviceRes);

        mockMvc.perform(get("/api/calendar/today")
                        .with(authentication(authWithRole("USER", 20L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.date").value("2026-01-21"))
                .andExpect(jsonPath("$.data.items[0].eventId").value(100))
                .andExpect(jsonPath("$.data.items[0].eventType").value("PUBLIC"))
                .andExpect(jsonPath("$.timestamp").exists());

        // limit default=50 확인
        verify(calendarQueryService, times(1))
                .getMyTodayEvents(eq(50), isNull(), isNull(), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("GET /api/calendar/today: cursorStart/cursorId 전달 -> 매핑 및 전달 검증")
    void today_success_withCursor() throws Exception {
        TodayEventsResponse serviceRes = new TodayEventsResponse(
                LocalDate.of(2026, 1, 21),
                List.of(),
                LocalDateTime.of(2026, 1, 21, 12, 0),
                999L
        );

        when(calendarQueryService.getMyTodayEvents(anyInt(), any(), any(), any(UserPrincipal.class)))
                .thenReturn(serviceRes);

        mockMvc.perform(get("/api/calendar/today")
                        .with(authentication(authWithRole("USER", 20L)))
                        .param("limit", "20")
                        .param("cursorStart", "2026-01-21T10:00:00")
                        .param("cursorId", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(calendarQueryService, times(1))
                .getMyTodayEvents(eq(20),
                        eq(LocalDateTime.of(2026, 1, 21, 10, 0)),
                        eq(123L),
                        any(UserPrincipal.class));
    }

    @Test
    @DisplayName("GET /api/calendar/today: Service IllegalArgumentException -> 400 + VALIDATION_ERROR")
    void today_service_illegalArgument_mapsTo400() throws Exception {
        when(calendarQueryService.getMyTodayEvents(anyInt(), any(), any(), any(UserPrincipal.class)))
                .thenThrow(new IllegalArgumentException("cursorStart와 cursorId는 함께 전달되어야 합니다."));

        mockMvc.perform(get("/api/calendar/today")
                        .with(authentication(authWithRole("USER", 20L)))
                        .param("cursorStart", "2026-01-21T10:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("cursorStart와 cursorId는 함께 전달되어야 합니다."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/calendar/today: Service ForbiddenException -> 403")
    void today_service_forbidden_mapsTo403() throws Exception {
        when(calendarQueryService.getMyTodayEvents(anyInt(), any(), any(), any(UserPrincipal.class)))
                .thenThrow(new ForbiddenException("권한이 없습니다."));

        mockMvc.perform(get("/api/calendar/today")
                        .with(authentication(authWithRole("USER", 20L))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(Matchers.not(Matchers.isEmptyOrNullString())))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }
}
