package com.moirai.alloc.calendar.query.controller;

import com.moirai.alloc.calendar.command.dto.response.CalendarViewResponse;
import com.moirai.alloc.calendar.query.service.CalendarQueryService;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CalendarController.class)
@Import(CalendarControllerWebMvcTest.TestSecurityConfig.class)
// 필요 시 Global Exception Handler import 추가
class CalendarControllerWebMvcTest {

    @MockBean
    private CalendarQueryService calendarQueryService;

    @Autowired
    MockMvc mockMvc;


    private RequestPostProcessor userPrincipal(Long userId, String role) {
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.userId()).thenReturn(userId);
        when(principal.role()).thenReturn(role);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        return SecurityMockMvcRequestPostProcessors.authentication(auth);
    }

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            http.httpBasic(Customizer.withDefaults());
            return http.build();
        }
    }

    @Test
    @DisplayName("GET /calendar: 성공 -> 200 + 파라미터 매핑(from/to/view) + Service 호출 검증")
    void getCalendarView_success() throws Exception {
        Long projectId = 1L;
        Long userId = 20L;

        when(calendarQueryService.getCalendarView(eq(projectId), any(LocalDate.class), any(LocalDate.class), anyString(), any(UserPrincipal.class)))
                .thenReturn(CalendarViewResponse.builder().items(List.of()).build());

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<String> viewCaptor = ArgumentCaptor.forClass(String.class);

        mockMvc.perform(get("/api/projects/{projectId}/calendar", projectId)
                        .with(userPrincipal(userId, "USER"))
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31")
                        .param("view", "month"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        // ApiResponse 포맷에 따라 조정
        // .andExpect(jsonPath("$.success").value(true))
        // .andExpect(jsonPath("$.data.items").isArray());

        verify(calendarQueryService, times(1))
                .getCalendarView(eq(projectId), fromCaptor.capture(), toCaptor.capture(), viewCaptor.capture(), any(UserPrincipal.class));

        assertThat(fromCaptor.getValue()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(toCaptor.getValue()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(viewCaptor.getValue()).isEqualTo("month");
    }

    @Test
    @DisplayName("GET /calendar: 파라미터 누락(from) -> 400 (RequestParam 매핑/검증)")
    void getCalendarView_missingParam_from() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/calendar", 1L)
                        .with(userPrincipal(20L, "USER"))
                        .param("to", "2026-01-31"))
                .andExpect(status().isBadRequest());
        verify(calendarQueryService, never()).getCalendarView(anyLong(), any(), any(), anyString(), any());
    }

    @Test
    @DisplayName("GET /calendar: from > to 를 Service에서 IllegalArgumentException -> 400 매핑 확인(예외 처리)")
    void getCalendarView_invalidRange_mapsTo400() throws Exception {
        when(calendarQueryService.getCalendarView(eq(1L), any(), any(), anyString(), any()))
                .thenThrow(new IllegalArgumentException("조회 시작일(from)은 종료일(to)보다 이후일 수 없습니다."));

        mockMvc.perform(get("/api/projects/{projectId}/calendar", 1L)
                        .with(userPrincipal(20L, "USER"))
                        .param("from", "2026-02-01")
                        .param("to", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /calendar: Service가 ForbiddenException -> 403 (Global Exception Handler 적용 확인)")
    void getCalendarView_forbidden_mapsTo403() throws Exception {
        when(calendarQueryService.getCalendarView(eq(1L), any(), any(), anyString(), any()))
                .thenThrow(new ForbiddenException("프로젝트 참여자가 아닙니다."));

        mockMvc.perform(get("/api/projects/{projectId}/calendar", 1L)
                        .with(userPrincipal(20L, "USER"))
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /calendar: 미인증 -> 401")
    void getCalendarView_unauthenticated_401() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/calendar", 1L)
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isUnauthorized());
    }
}
