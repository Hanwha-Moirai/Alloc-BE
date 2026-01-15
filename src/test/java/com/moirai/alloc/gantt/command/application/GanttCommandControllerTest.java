package com.moirai.alloc.gantt.command.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Sql(scripts = "/sql/gantt/controller_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class GanttCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("PM 권한으로 태스크 생성이 성공한다.")
    void createTask_returnsCreatedId() throws Exception {
        String body = """
                {
                  "milestoneId": 99100,
                  "assigneeId": 99101,
                  "taskCategory": "DEVELOPMENT",
                  "taskName": "New Task",
                  "taskDescription": "desc",
                  "startDate": "2025-01-03",
                  "endDate": "2025-01-04"
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/tasks", 99100)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    @DisplayName("PM 권한으로 태스크 수정이 성공한다.")
    void updateTask_returnsOk() throws Exception {
        String body = """
                {
                  "taskName": "Updated Task"
                }
                """;

        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}", 99100, 99100)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PM 권한으로 태스크 삭제가 성공한다.")
    void deleteTask_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/tasks/{taskId}", 99100, 99100)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PM 권한이 없으면 태스크 수정이 금지된다.")
    void updateTask_forbiddenWhenUserRoleIsNotPm() throws Exception {
        String body = """
                {
                  "taskName": "Updated Task"
                }
                """;

        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}", 99100, 99100)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("담당자가 아니면 태스크 완료가 금지된다.")
    void completeTask_forbiddenWhenRequesterIsNotAssignee() throws Exception {
        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}/complete", 99100, 99100)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("담당자일 때 태스크 완료가 성공한다.")
    void completeTask_returnsOkWhenRequesterIsAssignee() throws Exception {
        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}/complete", 99100, 99100)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(assigneeAuth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private Authentication pmAuth() {
        UserPrincipal principal = new UserPrincipal(
                99100L,
                "pm_99100",
                "pm99100@example.com",
                "PM User",
                "PM",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private Authentication userAuth() {
        UserPrincipal principal = new UserPrincipal(
                99100L,
                "user_99100",
                "user99100@example.com",
                "User",
                "USER",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private Authentication assigneeAuth() {
        UserPrincipal principal = new UserPrincipal(
                99101L,
                "user_99101",
                "user99101@example.com",
                "User One",
                "USER",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
