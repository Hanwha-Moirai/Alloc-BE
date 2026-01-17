package com.moirai.alloc.notification.command.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.notification.command.service.NotificationCommandService;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.Resource;

import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationCommandController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(NotificationCommandControllerWebMvcTest.TestSecurityConfig.class)
@DisplayName("Stage2 - NotificationCommandController (WebMvcTest)")
class NotificationCommandControllerWebMvcTest {

    @Resource MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    NotificationCommandService commandService;

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            http.httpBasic(Customizer.withDefaults());
            return http.build();
        }
    }

    private static UserPrincipal principal(long userId, String role) {
        return new UserPrincipal(
                userId,
                "login-" + userId,
                "u" + userId + "@test.com",
                "tester",
                role,           // "USER" / "PM" / "ADMIN"
                "pw"
        );
    }

    @Nested
    @DisplayName("PATCH /api/notifications/{id}/read")
    class MarkRead {

        @Test
        @DisplayName("200 OK - ROLE_USER + UserPrincipal 주입 성공 시 서비스 호출")
        void ok() throws Exception {
            mockMvc.perform(patch("/api/notifications/{id}/read", 10L)
                            .with(user(principal(1L, "USER"))))
                    .andExpect(status().isOk());

            verify(commandService, times(1)).markRead(1L, 10L);
        }

        @Test
        @DisplayName("404 NotFound - 서비스에서 ResponseStatusException(NOT_FOUND) 던지면 그대로 반영")
        void notFound_from_service() throws Exception {
            doThrow(new ResponseStatusException(NOT_FOUND, "알림을 찾을 수 없습니다."))
                    .when(commandService).markRead(1L, 999L);

            mockMvc.perform(patch("/api/notifications/{id}/read", 999L)
                            .with(user(principal(1L, "USER"))))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("401 Unauthorized - 미인증")
        void unauthorized() throws Exception {
            mockMvc.perform(patch("/api/notifications/{id}/read", 10L))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("403 Forbidden - Role 불일치(ROLE_GUEST)")
        void forbidden_role_mismatch() throws Exception {
            mockMvc.perform(patch("/api/notifications/{id}/read", 10L)
                            .with(user(principal(1L, "GUEST")))) // ROLE_GUEST
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 Forbidden - @WithMockUser는 principal 타입이 UserPrincipal이 아니어서 주입 실패 → controller에서 AccessDenied 발생")
        @WithMockUser(roles = "USER")
        void principal_injection_failed_should_be_403() throws Exception {
            mockMvc.perform(patch("/api/notifications/{id}/read", 10L))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/notifications/read-all")
    class MarkAllRead {

        @Test
        @DisplayName("200 OK - 전체 읽음 처리")
        void ok() throws Exception {
            mockMvc.perform(patch("/api/notifications/read-all")
                            .with(user(principal(1L, "USER"))))
                    .andExpect(status().isOk());

            verify(commandService, times(1)).markAllRead(1L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/notifications/{id}")
    class DeleteOne {

        @Test
        @DisplayName("200 OK - 단건 삭제")
        void ok() throws Exception {
            mockMvc.perform(delete("/api/notifications/{id}", 10L)
                            .with(user(principal(1L, "USER"))))
                    .andExpect(status().isOk());

            verify(commandService, times(1)).deleteNotification(1L, 10L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/notifications/read")
    class DeleteAllRead {

        @Test
        @DisplayName("200 OK - 읽음 처리된 알림 전체 삭제")
        void ok() throws Exception {
            mockMvc.perform(delete("/api/notifications/read")
                            .with(user(principal(1L, "USER"))))
                    .andExpect(status().isOk());

            verify(commandService, times(1)).deleteAllRead(1L);
        }
    }
}
