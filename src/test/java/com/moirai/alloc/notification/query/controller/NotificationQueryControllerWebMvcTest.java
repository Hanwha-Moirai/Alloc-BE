package com.moirai.alloc.notification.query.controller;

import com.moirai.alloc.common.dto.pagination.Pagination;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.notification.query.dto.response.NotificationPageResponse;
import com.moirai.alloc.notification.query.dto.response.NotificationSummaryResponse;
import com.moirai.alloc.notification.query.service.NotificationQueryService;
import com.moirai.alloc.notification.query.sse.NotificationSseEmitters;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @org.springframework.boot.test.mock.mockito.MockBean
    NotificationSseEmitters emitters;

    @AfterEach
    void resetMocks() {
        // 테스트 간 Mock 호출/스텁이 누적되지 않도록 격리
        Mockito.reset(queryService, emitters);
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
    @DisplayName("GET /api/notifications/stream (SSE)")
    class Stream {

        @Test
        @DisplayName("200 OK - emitter 등록 + 초기 UNREAD_COUNT push 호출 검증")
        void ok_stream_register_and_push_unread() throws Exception {
            // SSE는 비동기 응답이므로, complete()로 종료시킨 뒤 asyncDispatch로 최종 응답을 검증한다.
            SseEmitter emitter = new SseEmitter(10_000L);

            given(emitters.add(1L)).willReturn(emitter);
            given(queryService.getMyUnreadCount(1L)).willReturn(2L);

            MvcResult mvcResult = mockMvc.perform(get("/api/notifications/stream")
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .with(user(principal(1L, "USER"))))
                    .andExpect(status().isOk())
                    .andExpect(request().asyncStarted()) // SSE는 async 처리 시작이 정상
                    .andReturn();

            // async 처리를 완료시켜 Content-Type 등 응답 메타데이터 검증이 가능하게 함
            emitter.complete();

            mockMvc.perform(asyncDispatch(mvcResult))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

            verify(emitters, times(1)).add(1L);
            verify(queryService, times(1)).getMyUnreadCount(1L);
            verify(emitters, times(1)).sendToUser(eq(1L), eq("UNREAD_COUNT"), eq(2L));
        }
    }
}
