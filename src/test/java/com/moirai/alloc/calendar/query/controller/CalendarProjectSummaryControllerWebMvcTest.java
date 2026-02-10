package com.moirai.alloc.calendar.query.controller;

import com.moirai.alloc.calendar.query.dto.ProjectUpcomingEventItemResponse;
import com.moirai.alloc.calendar.query.dto.ProjectUpcomingEventsResponse;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = CalendarProjectSummaryController.class)
@Import({
        GlobalExceptionHandler.class,
        CalendarProjectSummaryControllerWebMvcTest.TestSecurityConfig.class,
        CalendarProjectSummaryControllerWebMvcTest.SecurityExceptionAdvice.class
})
class CalendarProjectSummaryControllerWebMvcTest {

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
    @DisplayName("GET /api/projects/{projectId}/calendar/upcoming: 성공(default limit=20)")
    void upcoming_success_defaultLimit() throws Exception {
        Long projectId = 1L;

        ProjectUpcomingEventsResponse serviceRes = new ProjectUpcomingEventsResponse(
                projectId,
                List.of(new ProjectUpcomingEventItemResponse(
                        10L,
                        com.moirai.alloc.calendar.command.domain.entity.EventType.PUBLIC,
                        "공유 일정",
                        "공유 회의",
                        LocalDateTime.of(2026, 1, 22, 10, 0),
                        LocalDateTime.of(2026, 1, 22, 11, 0),
                        1
                )),
                null,
                null
        );

        when(calendarQueryService.getProjectUpcomingEvents(eq(projectId), anyInt(), any(), any(), any(UserPrincipal.class)))
                .thenReturn(serviceRes);

        mockMvc.perform(get("/api/projects/{projectId}/calendar/upcoming", projectId)
                        .with(authentication(authWithRole("USER", 20L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.items[0].label").value("공유 일정"))
                .andExpect(jsonPath("$.data.items[0].eventType").value("PUBLIC"))
                .andExpect(jsonPath("$.data.items[0].startDate").value("2026-01-22T10:00:00"))
                .andExpect(jsonPath("$.data.items[0].endDate").value("2026-01-22T11:00:00"))
                .andExpect(jsonPath("$.timestamp").exists());

        // limit default=20 확인
        verify(calendarQueryService, times(1))
                .getProjectUpcomingEvents(eq(projectId), eq(20), isNull(), isNull(), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("GET /upcoming: cursorStart/cursorId 전달 -> 매핑 및 전달 검증")
    void upcoming_success_withCursor() throws Exception {
        Long projectId = 1L;

        when(calendarQueryService.getProjectUpcomingEvents(eq(projectId), anyInt(), any(), any(), any(UserPrincipal.class)))
                .thenReturn(new ProjectUpcomingEventsResponse(projectId, List.of(), null, null));

        mockMvc.perform(get("/api/projects/{projectId}/calendar/upcoming", projectId)
                        .with(authentication(authWithRole("USER", 20L)))
                        .param("limit", "5")
                        .param("cursorStart", "2026-01-21T10:00:00")
                        .param("cursorId", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(calendarQueryService, times(1))
                .getProjectUpcomingEvents(eq(projectId),
                        eq(5),
                        eq(LocalDateTime.of(2026, 1, 21, 10, 0)),
                        eq(123L),
                        any(UserPrincipal.class));
    }

    @Test
    @DisplayName("GET /upcoming: Service IllegalArgumentException -> 400 + VALIDATION_ERROR")
    void upcoming_service_illegalArgument_mapsTo400() throws Exception {
        Long projectId = 1L;

        when(calendarQueryService.getProjectUpcomingEvents(eq(projectId), anyInt(), any(), any(), any(UserPrincipal.class)))
                .thenThrow(new IllegalArgumentException("cursorStart와 cursorId는 함께 전달되어야 합니다."));

        mockMvc.perform(get("/api/projects/{projectId}/calendar/upcoming", projectId)
                        .with(authentication(authWithRole("USER", 20L)))
                        .param("cursorStart", "2026-01-21T10:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("cursorStart와 cursorId는 함께 전달되어야 합니다."));
    }

    @Test
    @DisplayName("GET /upcoming: Service ForbiddenException -> 403")
    void upcoming_service_forbidden_mapsTo403() throws Exception {
        Long projectId = 1L;

        when(calendarQueryService.getProjectUpcomingEvents(eq(projectId), anyInt(), any(), any(), any(UserPrincipal.class)))
                .thenThrow(new ForbiddenException("프로젝트 참여자가 아닙니다."));

        mockMvc.perform(get("/api/projects/{projectId}/calendar/upcoming", projectId)
                        .with(authentication(authWithRole("USER", 20L))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(Matchers.not(Matchers.isEmptyOrNullString())))
                .andExpect(jsonPath("$.message").value("프로젝트 참여자가 아닙니다."));
    }
}
