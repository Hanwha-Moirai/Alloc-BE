package com.moirai.alloc.gantt.query.application;

import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import com.moirai.alloc.gantt.query.dto.request.TaskSearchRequest;
import com.moirai.alloc.gantt.query.dto.response.MilestoneResponse;
import com.moirai.alloc.gantt.query.dto.response.TaskResponse;
import org.junit.jupiter.api.DisplayName;
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
    private static final Long USER_ID = 99002L;

    @Autowired
    private GanttQueryService ganttQueryService;

    @Test
    @DisplayName("프로젝트 태스크 목록 조회에 성공했습니다.")
    void findTasks_returnsProjectTasks() {
        List<TaskResponse> responses = ganttQueryService.findTasks(
                PROJECT_ID,
                new TaskSearchRequest(null, null, null)
        );

        assertThat(responses).isNotEmpty();
        assertThat(responses.stream().anyMatch(task -> task.taskId().equals(99001L))).isTrue();
    }

    @Test
    @DisplayName("태스크 목록 조회 시 사용자 이름을 포함한다.")
    void findTasks_returnsUserName() {
        List<TaskResponse> responses = ganttQueryService.findTasks(
                PROJECT_ID,
                new TaskSearchRequest(null, null, null)
        );

        assertThat(responses).hasSize(2);
        assertThat(responses.stream().allMatch(task -> "User Two".equals(task.userName()))).isTrue();
    }

    @Test
    @DisplayName("마일스톤 상세 조회에 성공하고 태스크가 포함된다.")
    void findMilestone_returnsMilestoneWithTasks() {
        MilestoneResponse response = ganttQueryService.findMilestone(PROJECT_ID, 99001L);

        assertThat(response.milestoneId()).isEqualTo(99001L);
        assertThat(response.tasks()).isNotEmpty();
        assertThat(response.tasks().stream().anyMatch(task -> task.taskId().equals(99001L))).isTrue();
    }

    @Test
    @DisplayName("마일스톤 목록 조회 시 태스크가 마일스톤별로 묶인다.")
    void findMilestones_groupsTasksByMilestone() {
        List<MilestoneResponse> responses = ganttQueryService.findMilestones(PROJECT_ID);

        Map<Long, Integer> taskCounts = responses.stream()
                .collect(Collectors.toMap(MilestoneResponse::milestoneId, item -> item.tasks().size()));

        assertThat(taskCounts.get(99001L)).isGreaterThanOrEqualTo(1);
        assertThat(taskCounts.get(99002L)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("삭제된 마일스톤은 목록에서 제외된다.")
    void findMilestones_excludesDeletedMilestones() {
        List<MilestoneResponse> responses = ganttQueryService.findMilestones(PROJECT_ID);

        assertThat(responses.stream().anyMatch(item -> item.milestoneId().equals(99003L))).isFalse();
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
