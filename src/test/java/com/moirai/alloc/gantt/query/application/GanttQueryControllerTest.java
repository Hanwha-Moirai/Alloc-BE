package com.moirai.alloc.gantt.query.application;

import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Sql(scripts = "/sql/gantt/controller_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class GanttQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void findTasks_returnsTasks() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/tasks", 99100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].taskId").value(99100));
    }

    @Test
    @WithMockUser
    void findMilestone_returnsMilestone() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/ganttchart/milestones/{milestoneId}", 99100, 99100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.milestoneId").value(99100));
    }

    @Test
    @WithMockUser
    void findMilestones_returnsList() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/ganttchart/milestones", 99100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @TestConfiguration
    static class TestAuthConfig {
        @Bean
        @Primary
        AuthenticatedUserProvider authenticatedUserProvider() {
            return () -> 99100L;
        }
    }
}
