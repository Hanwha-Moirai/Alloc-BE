package com.moirai.alloc.gantt.query.application;

import com.moirai.alloc.gantt.common.security.AuthenticatedUserProvider;
import com.moirai.alloc.gantt.command.domain.entity.Task.TaskCategory;
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
@Sql(scripts = "/sql/gantt/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
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
                new TaskSearchRequest(null, null, null, null, null)
        );

        assertThat(responses).isNotEmpty();
        assertThat(responses.stream().anyMatch(task -> task.taskId().equals(99001L))).isTrue();
    }

    @Test
    @DisplayName("태스크 목록 조회 시 사용자 이름을 포함한다.")
    void findTasks_returnsUserName() {
        List<TaskResponse> responses = ganttQueryService.findTasks(
                PROJECT_ID,
                new TaskSearchRequest(null, null, null, null, null)
        );

        assertThat(responses).hasSize(2);
        assertThat(responses.stream().allMatch(task -> "User Two".equals(task.userName()))).isTrue();
    }

    @Test
    @DisplayName("태스크 유형 필터가 적용된다.")
    void findTasks_filtersByCategory() {
        List<TaskResponse> responses = ganttQueryService.findTasks(
                PROJECT_ID,
                new TaskSearchRequest(null, null, null, List.of(TaskCategory.TESTING), null)
        );

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).taskId()).isEqualTo(99002L);
    }

    @Test
    @DisplayName("담당자 이름 필터가 적용된다.")
    void findTasks_filtersByAssigneeName() {
        List<TaskResponse> responses = ganttQueryService.findTasks(
                PROJECT_ID,
                new TaskSearchRequest(null, null, null, null, List.of("User Two"))
        );

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("담당자 이름이 없으면 전체 태스크가 조회된다.")
    void findTasks_whenAssigneeMissing_returnsAll() {
        List<TaskResponse> responses = ganttQueryService.findTasks(
                PROJECT_ID,
                new TaskSearchRequest(null, null, null, null, null)
        );

        assertThat(responses).hasSize(2);
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

        assertThat(taskCounts.get(99001L)).isEqualTo(1);
        assertThat(taskCounts.get(99002L)).isEqualTo(1);
    }

    @Test
    @DisplayName("삭제된 마일스톤은 목록에서 제외된다.")
    void findMilestones_excludesDeletedMilestones() {
        List<MilestoneResponse> responses = ganttQueryService.findMilestones(PROJECT_ID);

        assertThat(responses.stream().anyMatch(item -> item.milestoneId().equals(99003L))).isFalse();
    }

    @Test
    @DisplayName("프로젝트 마일스톤 달성률을 계산한다.")
    void findMilestoneCompletionRate_returnsRate() {
        Double rate = ganttQueryService.findMilestoneCompletionRate(PROJECT_ID);

        assertThat(rate).isEqualTo(50.0);
    }

    @TestConfiguration
    static class TestAuthConfig {
        @Bean
        @Primary
        AuthenticatedUserProvider authenticatedUserProvider() {
            return new AuthenticatedUserProvider() {
                @Override
                public Long getCurrentUserId() {
                    return USER_ID;
                }

                @Override
                public String getCurrentUserRole() {
                    return "USER";
                }
            };
        }
    }
}
