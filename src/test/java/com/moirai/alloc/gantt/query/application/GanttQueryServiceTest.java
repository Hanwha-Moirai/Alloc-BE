package com.moirai.alloc.gantt.query.application;

import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import com.moirai.alloc.gantt.query.dto.request.TaskSearchRequest;
import com.moirai.alloc.gantt.query.dto.response.MilestoneResponse;
import com.moirai.alloc.gantt.query.dto.response.TaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Sql(scripts = "/sql/gantt/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class GanttQueryServiceTest {

    private static final Long PROJECT_ID = 99001L;
    private static final Long USER_ID = 99001L;

    @Autowired
    private GanttQueryService ganttQueryService;

    @Test
    void findTasks_returnsProjectTasks() {
        List<TaskResponse> responses = ganttQueryService.findTasks(
                PROJECT_ID,
                new TaskSearchRequest(null, null, null, null)
        );

        assertThat(responses).isNotEmpty();
        assertThat(responses.stream().anyMatch(task -> task.taskId().equals(99001L))).isTrue();
    }

    @Test
    void findMilestone_returnsMilestoneWithTasks() {
        MilestoneResponse response = ganttQueryService.findMilestone(PROJECT_ID, 99001L);

        assertThat(response.milestoneId()).isEqualTo(99001L);
        assertThat(response.tasks()).hasSize(1);
    }

    @Test
    void findMilestones_groupsTasksByMilestone() {
        List<MilestoneResponse> responses = ganttQueryService.findMilestones(PROJECT_ID);

        Map<Long, Integer> taskCounts = responses.stream()
                .collect(Collectors.toMap(MilestoneResponse::milestoneId, item -> item.tasks().size()));

        assertThat(taskCounts.get(99001L)).isEqualTo(1);
        assertThat(taskCounts.get(99002L)).isEqualTo(1);
    }

    @TestConfiguration
    static class TestAuthConfig {
        @Bean
        @Primary
        AuthenticatedUserProvider authenticatedUserProvider() {
            return () -> USER_ID;
        }
    }
}
