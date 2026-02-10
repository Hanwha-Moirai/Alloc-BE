package com.moirai.alloc.gantt.query;

import com.moirai.alloc.gantt.query.application.GanttQueryService;
import com.moirai.alloc.gantt.query.dto.response.TaskResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = "mybatis.mapper-locations=classpath*:mapper/**/*.xml")
@Sql(scripts = "/sql/gantt/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/gantt/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserTaskQueryServiceTest {

    @Autowired
    private GanttQueryService ganttQueryService;

    @Test
    @DisplayName("사용자 미완료 태스크 목록 조회에 성공한다.")
    void findIncompleteTasksByUserId_returnsOnlyIncompleteTasks() {
        List<TaskResponse> responses = ganttQueryService.findIncompleteTasksByUserId(99002L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).taskId()).isEqualTo(99001L);
        assertThat(responses.get(0).isCompleted()).isFalse();
    }

    @TestConfiguration
    static class TestMailConfig {
        @Bean
        @Primary
        JavaMailSender javaMailSender() {
            return new JavaMailSenderImpl();
        }
    }
}
