package com.moirai.alloc.gantt.query;

import com.moirai.alloc.gantt.query.application.DelayedTaskQueryService;
import com.moirai.alloc.gantt.query.dto.request.DelayedTaskSearchRequest;
import com.moirai.alloc.gantt.query.dto.response.DelayedTaskResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class DelayedTaskQueryServiceTest {

    @Autowired
    private DelayedTaskQueryService delayedTaskQueryService;

    @Test
    @DisplayName("지연 태스크 목록을 조회한다.")
    @Sql(scripts = "/sql/gantt/delayed_tasks_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/gantt/delayed_tasks_cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void findDelayedTasks_returnsDelayedTasks() {
        DelayedTaskSearchRequest request = new DelayedTaskSearchRequest(null, null, null, null);

        List<DelayedTaskResponse> responses = delayedTaskQueryService.findDelayedTasks(request);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(DelayedTaskResponse::taskName)
                .containsExactlyInAnyOrder("Delay Task Alpha", "Delay Task Beta");
    }

    @Test
    @DisplayName("조건으로 지연 태스크를 필터링한다.")
    @Sql(scripts = "/sql/gantt/delayed_tasks_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/gantt/delayed_tasks_cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void findDelayedTasks_filtersByCondition() {
        DelayedTaskSearchRequest request = new DelayedTaskSearchRequest(
                "Alpha",
                "Delay Project A",
                "Delay User A",
                7
        );

        List<DelayedTaskResponse> responses = delayedTaskQueryService.findDelayedTasks(request);

        assertThat(responses).hasSize(1);
        DelayedTaskResponse response = responses.get(0);
        assertThat(response.taskName()).isEqualTo("Delay Task Alpha");
        assertThat(response.projectName()).isEqualTo("Delay Project A");
        assertThat(response.assigneeName()).isEqualTo("Delay User A");
        assertThat(response.delayedDays()).isGreaterThanOrEqualTo(7);
    }
}
