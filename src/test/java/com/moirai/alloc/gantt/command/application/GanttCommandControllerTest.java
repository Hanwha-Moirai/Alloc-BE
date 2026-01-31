package com.moirai.alloc.gantt.command.application;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import com.moirai.alloc.auth.cookie.AuthCookieProperties;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Sql(scripts = "/sql/gantt/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/gantt/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class GanttCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthCookieProperties authCookieProperties;

    @Test
    @DisplayName("PM 권한으로 태스크 생성이 성공한다.")
    void createTask_returnsCreatedId() throws Exception {
        String body = """
                {
                  "milestoneId": 99001,
                  "assigneeId": 99002,
                  "taskCategory": "DEVELOPMENT",
                  "taskName": "New Task",
                  "taskDescription": "desc",
                  "startDate": "2025-01-03",
                  "endDate": "2025-01-04"
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/tasks", 99001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    @DisplayName("PM 권한으로 태스크 삭제가 성공한다.")
    void deleteTask_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/tasks/{taskId}", 99001, 99001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .with(csrfToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private Authentication pmAuth() {
        UserPrincipal principal = new UserPrincipal(
                99001L,
                "pm_99001",
                "pm99001@example.com",
                "PM User",
                "PM",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private Authentication userAuth() {
        UserPrincipal principal = new UserPrincipal(
                99001L,
                "user_99001",
                "user99001@example.com",
                "User",
                "USER",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private Authentication assigneeAuth() {
        UserPrincipal principal = new UserPrincipal(
                99002L,
                "user_99002",
                "user99002@example.com",
                "User Two",
                "USER",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private RequestPostProcessor csrfToken() {
        return request -> {
            String token = "test-csrf-token";
            request.setCookies(new Cookie(authCookieProperties.getCsrfTokenName(), token));
            request.addHeader("X-CSRF-Token", token);
            return request;
        };
    }
}
