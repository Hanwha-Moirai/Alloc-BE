package com.moirai.alloc.report.command.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Sql(scripts = "/sql/report/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/report/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class WeeklyReportDocsCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("주간보고 생성 요청이 성공한다.")
    void createReport_returnsCreatedResponse() throws Exception {
        mockMvc.perform(post("/api/projects/{projectId}/docs/report/create", 77001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").isNumber());
    }

    @Test
    @DisplayName("주간보고 수정 요청이 성공한다.")
    void saveReport_returnsUpdatedAt() throws Exception {
        String body = """
                {
                  "reportId": 77001,
                  "reportStatus": "REVIEWED",
                  "changeOfPlan": "변경",
                  "taskCompletionRate": 0.8,
                  "completedTasks": [{"taskId": 77001}],
                  "incompleteTasks": [{"taskId": 77002, "delayReason": "지연"}],
                  "nextWeekTasks": [{"taskId": 77003, "plannedStartDate": "2025-01-13", "plannedEndDate": "2025-01-17"}]
                }
                """;

        mockMvc.perform(patch("/api/projects/{projectId}/docs/report/save", 77001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").value(77001));
    }

    @Test
    @DisplayName("주간보고 삭제 요청이 성공한다.")
    void deleteReport_returnsOk() throws Exception {
        String body = """
                {
                  "reportId": 77001
                }
                """;

        mockMvc.perform(delete("/api/projects/{projectId}/docs/report/delete", 77001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").value(77001));
    }

    @Test
    @DisplayName("PM이 아니면 주간보고 수정 요청이 거부된다.")
    void saveReport_forbiddenWhenUserRoleIsNotPm() throws Exception {
        String body = """
                {
                  "reportId": 77001,
                  "reportStatus": "REVIEWED",
                  "changeOfPlan": "변경",
                  "taskCompletionRate": 0.8,
                  "completedTasks": [{"taskId": 77001}],
                  "incompleteTasks": [{"taskId": 77002, "delayReason": "지연"}],
                  "nextWeekTasks": [{"taskId": 77003, "plannedStartDate": "2025-01-13", "plannedEndDate": "2025-01-17"}]
                }
                """;

        mockMvc.perform(patch("/api/projects/{projectId}/docs/report/save", 77001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    private Authentication pmAuth() {
        UserPrincipal principal = new UserPrincipal(
                77001L,
                "pm_77001",
                "pm77001@example.com",
                "PM User",
                "PM",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private Authentication userAuth() {
        UserPrincipal principal = new UserPrincipal(
                77002L,
                "user_77002",
                "user77002@example.com",
                "User",
                "USER",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
