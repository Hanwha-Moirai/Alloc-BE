package com.moirai.alloc.notification.query.controller;

import com.moirai.alloc.common.dto.pagination.Pagination;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.notification.query.dto.response.NotificationPageResponse;
import com.moirai.alloc.notification.query.dto.response.NotificationPollResponse;
import com.moirai.alloc.notification.query.dto.response.NotificationSummaryResponse;
import com.moirai.alloc.notification.query.service.NotificationQueryService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationQueryController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(NotificationQueryControllerWebMvcTest.TestSecurityConfig.class)
@DisplayName("Stage2 - NotificationQueryController (WebMvcTest)")
class NotificationQueryControllerWebMvcTest {

    @Resource MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    NotificationQueryService queryService;

    @AfterEach
    void resetMocks() {
        // 테스트 간 Mock 호출/스텁이 누적되지 않도록 격리
        Mockito.reset(queryService);
    }

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());

            // 인증 없으면 401, 권한 없으면 403을 기대하기 위해 로그인/로그아웃 페이지 기능은 비활성화
            http.formLogin(form -> form.disable());
            http.logout(logout -> logout.disable());

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
                role,   // "USER" / "PM" / "ADMIN" / "GUEST"
                "pw"
        );
    }

    @Nested
    @DisplayName("GET /api/notifications")
    class GetMyNotifications {

        @Test
        @DisplayName("200 OK - page/size 파라미터 매핑 및 서비스 호출 검증")
        void ok_with_params() throws Exception {
            var item = NotificationSummaryResponse.builder()
                    .notificationId(10L)
                    .title("태스크 담당자 배정")
                    .content("태스크 A 담당자로 지정되었습니다.")
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .targetType(null)
                    .targetId(1L)
                    .linkUrl("/tasks/1")
                    .build();

            var pageRes = NotificationPageResponse.builder()
                    .notifications(List.of(item))
                    .pagination(Pagination.builder()
                            .currentPage(1)
                            .totalPages(10)
                            .totalItems(100)
                            .build())
                    .build();

            given(queryService.getMyNotifications(1L, 1, 20)).willReturn(pageRes);

            mockMvc.perform(get("/api/notifications")
                            .param("page", "1")
                            .param("size", "20")
                            .with(user(principal(1L, "USER"))))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("notifications")))
                    .andExpect(content().string(containsString("pagination")));

            verify(queryService, times(1)).getMyNotifications(1L, 1, 20);
        }

        @Test
        @DisplayName("200 OK - page 미지정이면 defaultValue=0 매핑")
        void ok_default_page() throws Exception {
            given(queryService.getMyNotifications(1L, 0, null))
                    .willReturn(NotificationPageResponse.builder()
                            .notifications(List.of())
                            .pagination(Pagination.builder().currentPage(0).totalPages(0).totalItems(0).build())
                            .build());

            mockMvc.perform(get("/api/notifications")
                            .with(user(principal(1L, "USER"))))
                    .andExpect(status().isOk());

            verify(queryService, times(1)).getMyNotifications(1L, 0, null);
        }

        @Test
        @DisplayName("401 Unauthorized - 미인증")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/notifications"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("403 Forbidden - Role 불일치(ROLE_GUEST)")
        void forbidden_role_mismatch() throws Exception {
            mockMvc.perform(get("/api/notifications")
                            .with(user(principal(1L, "GUEST"))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread-count")
    class UnreadCount {

        @Test
        @DisplayName("200 OK - 미읽음 카운트 반환 + 서비스 호출 검증")
        void ok() throws Exception {
            given(queryService.getMyUnreadCount(1L)).willReturn(2L);

            mockMvc.perform(get("/api/notifications/unread-count")
                            .with(user(principal(1L, "USER"))))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("2")));

            verify(queryService, times(1)).getMyUnreadCount(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/poll")
    class Poll {

        @Test
        @DisplayName("200 OK - sinceId/size 파라미터 매핑 및 서비스 호출 검증")
        void ok_poll_with_params() throws Exception {
            var item = NotificationSummaryResponse.builder()
                    .notificationId(11L)
                    .title("새 알림")
                    .content("새 알림 내용")
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .targetType(null)
                    .targetId(2L)
                    .linkUrl("/tasks/2")
                    .build();

            var pollRes = NotificationPollResponse.builder()
                    .notifications(List.of(item))
                    .unreadCount(3L)
                    .latestNotificationId(11L)
                    .build();

            given(queryService.pollMyNotifications(1L, 5L, 30)).willReturn(pollRes);

            mockMvc.perform(get("/api/notifications/poll")
                            .param("sinceId", "5")
                            .param("size", "30")
                            .with(user(principal(1L, "USER"))))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("notifications")))
                    .andExpect(content().string(containsString("unreadCount")))
                    .andExpect(content().string(containsString("latestNotificationId")));

            verify(queryService, times(1)).pollMyNotifications(1L, 5L, 30);
        }
    }
}
