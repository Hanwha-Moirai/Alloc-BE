package com.moirai.alloc.notification.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moirai.alloc.notification.command.dto.request.InternalNotificationCreateRequest;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.command.service.NotificationCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InternalNotificationController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(InternalNotificationControllerTest.TestSecurityConfig.class)
@DisplayName("Stage2 - InternalNotificationController (WebMvcTest)")
class InternalNotificationControllerTest {

    @Resource MockMvc mockMvc;
    @Resource ObjectMapper objectMapper;

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

    @Nested
    @DisplayName("POST /api/internal/notifications")
    class Create {

        @Test
        @DisplayName("201 Created - INTERNAL 권한이면 생성 성공 + Service 호출 + 응답에 createdCount 포함")
        @WithMockUser(authorities = "INTERNAL")
        void create_ok() throws Exception {
            String body = """
                {
                  "templateType": "TASK_ASSIGN",
                  "targetUserIds": [1, 2],
                  "variables": {"taskName": "API 구현"},
                  "targetType": "TASK",
                  "targetId": 10,
                  "linkUrl": "/tasks/10"
                }
                """;

            given(commandService.createInternalNotifications(any()))
                    .willReturn(InternalNotificationCreateResponse.builder()
                            .createdCount(2)
                            .alarmIds(java.util.List.of(100L, 101L))
                            .build());

            mockMvc.perform(post("/api/internal/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    // ApiResponse wrapper의 필드명은 프로젝트마다 다를 수 있어, 최소 계약(핵심 필드 존재)만 문자열로 확인
                    .andExpect(content().string(containsString("createdCount")));

            ArgumentCaptor<InternalNotificationCreateRequest> captor =
                    ArgumentCaptor.forClass(InternalNotificationCreateRequest.class);
            verify(commandService, times(1)).createInternalNotifications(captor.capture());

            InternalNotificationCreateRequest req = captor.getValue();
            assertThat(req.getTemplateType().name()).isEqualTo("TASK_ASSIGN");
            assertThat(req.getTargetUserIds()).containsExactly(1L, 2L);
            assertThat(req.getVariables().get("taskName")).isEqualTo("API 구현");
            assertThat(req.getTargetType().name()).isEqualTo("TASK");
            assertThat(req.getTargetId()).isEqualTo(10L);
            assertThat(req.getLinkUrl()).isEqualTo("/tasks/10");
        }

        @Test
        @DisplayName("400 BadRequest - Validation 실패(필수값 누락/빈 리스트)")
        @WithMockUser(authorities = "INTERNAL")
        void create_validation_400() throws Exception {
            String body = """
                {
                  "targetUserIds": [],
                  "targetType": "TASK",
                  "targetId": 10
                }
                """;

            mockMvc.perform(post("/api/internal/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("401 Unauthorized - 미인증")
        void create_unauthorized_401() throws Exception {
            mockMvc.perform(post("/api/internal/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("403 Forbidden - 인증은 되었지만 INTERNAL 권한 없음")
        @WithMockUser(roles = "USER")
        void create_forbidden_403() throws Exception {
            String validBody = """
        {
          "templateType": "TASK_ASSIGN",
          "targetUserIds": [1],
          "targetType": "TASK",
          "targetId": 10
        }
        """;

            mockMvc.perform(post("/api/internal/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody))
                    .andExpect(status().isForbidden());

            verify(commandService, never()).createInternalNotifications(any());
        }

    }
}
