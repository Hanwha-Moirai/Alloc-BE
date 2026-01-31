package com.moirai.alloc.report.query.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Sql(scripts = "/sql/report/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/report/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class WeeklyReportDocsQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("프로젝트 주간보고 목록을 조회한다.")
    void getReports_returnsPage() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/docs/report", 77001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].reporterName").value("PM User"))
                .andExpect(jsonPath("$.data.content[0].weekLabel").value("2025년 1월 2주차"));
    }

    @Test
    @DisplayName("프로젝트 주간보고 검색 결과를 반환한다.")
    void searchReports_returnsMatches() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/docs/report/search", 77001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .param("reportStatus", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].reportId").value(77001))
                .andExpect(jsonPath("$.data.content[0].reporterName").value("PM User"))
                .andExpect(jsonPath("$.data.content[0].weekLabel").value("2025년 1월 2주차"));
    }

    @Test
    @DisplayName("프로젝트 주간보고 상세를 조회한다.")
    void getReportDetail_returnsDetail() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/docs/report/{reportId}", 77001, 77001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").value(77001))
                .andExpect(jsonPath("$.data.reporterName").value("PM User"))
                .andExpect(jsonPath("$.data.weekLabel").value("2025년 1월 2주차"));
    }

    @Test
    @DisplayName("프로젝트 미작성 주차 목록을 조회한다.")
    void getMissingWeeks_returnsMissingWeeks() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/docs/report/missing", 77001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].weekStartDate").value("2024-12-29"))
                .andExpect(jsonPath("$.data[0].weekEndDate").value("2025-01-04"));
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
}
