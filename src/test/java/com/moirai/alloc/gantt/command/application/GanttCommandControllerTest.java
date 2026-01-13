package com.moirai.alloc.gantt.command.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@EnableJpaAuditing
//@TestPropertySource(properties = "mybatis.mapper-locations=classpath*:mapper/gantt/*.xml")
//@Import(com.moirai.alloc.gantt.config.GanttMybatisTestConfig.class)
@Sql(scripts = "/sql/gantt/controller_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class GanttCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    @WithMockUser
    void updateTask_returnsOk() throws Exception {
        String body = """
                {
                  "taskName": "Updated Task"
                }
                """;

        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}", 99100, 99100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void deleteTask_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/tasks/{taskId}", 99100, 99100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
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
