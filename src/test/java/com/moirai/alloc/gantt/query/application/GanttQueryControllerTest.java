package com.moirai.alloc.gantt.query.application;

import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Sql(scripts = "/sql/gantt/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/gantt/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class GanttQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("태스크 목록 조회가 성공하고 userName을 반환한다.")
    void findTasks_returnsTasks() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/tasks", 99001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].taskId").value(99001))
                .andExpect(jsonPath("$.data[0].userName").value("User Two"));
    }

    @Test
    @WithMockUser
    @DisplayName("마일스톤 상세 조회가 성공한다.")
    void findMilestone_returnsMilestone() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/ganttchart/milestones/{milestoneId}", 99001, 99001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.milestoneId").value(99001));
    }

    @Test
    @WithMockUser
    @DisplayName("삭제된 마일스톤을 제외한 목록 조회가 성공한다.")
    void findMilestones_returnsList() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/ganttchart/milestones", 99001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].milestoneId", not(hasItem(99003))));
    }

    @Test
    @WithMockUser
    @DisplayName("프로젝트 마일스톤 달성률 조회가 성공한다.")
    void findMilestoneCompletionRate_returnsRate() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/achievement-rate", 99001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(50.0));
    }

    @TestConfiguration
    static class TestAuthConfig {
        @Bean
        @Primary
        AuthenticatedUserProvider authenticatedUserProvider() {
            return () -> 99002L;
        }
    }
}
