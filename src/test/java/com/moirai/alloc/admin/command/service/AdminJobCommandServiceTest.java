package com.moirai.alloc.admin.command.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class
})
@Sql(scripts = "/sql/admin/job_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminJobCommandServiceTest {

    @Autowired
    private AdminJobCommandService adminJobCommandService;

    @Nested
    @DisplayName("직무 등록")
    class CreateJob {

        @Test
        @DisplayName("새로운 직무를 등록한다")
        void createJob_success() {
            Long jobId = adminJobCommandService.createJob("New Job");
            assertThat(jobId).isNotNull();
        }

        @Test
        @DisplayName("이미 존재하는 직무 이름이면 예외를 발생시킨다")
        void createJob_duplicateName_throwsException() {
            assertThatThrownBy(() -> adminJobCommandService.createJob("Backend Developer"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 직무입니다.");
        }

        @Test
        @DisplayName("직무 이름이 공백이면 예외를 발생시킨다")
        void createJob_blankName_throwsException() {
            assertThatThrownBy(() -> adminJobCommandService.createJob("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("직무 이름이 필요합니다.");
        }
    }

    @Nested
    @DisplayName("직무 수정")
    class UpdateJob {
        
        private static final Long JOB_ID_BACKEND = 99001L;
        
        @Test
        @DisplayName("직무 이름을 수정한다")
        void updateJob_success() {
            Long updatedJobId = adminJobCommandService.updateJob(JOB_ID_BACKEND, "Backend Engineer");
            assertThat(updatedJobId).isEqualTo(JOB_ID_BACKEND);
        }

        @Test
        @DisplayName("존재하지 않는 직무 ID이면 예외를 발생시킨다")
        void updateJob_notFound_throwsException() {
            assertThatThrownBy(() -> adminJobCommandService.updateJob(99999L, "NonExistent Job"))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("직무를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("다른 직무와 동일한 이름으로 수정하면 예외를 발생시킨다")
        void updateJob_duplicateName_throwsException() {
            assertThatThrownBy(() -> adminJobCommandService.updateJob(JOB_ID_BACKEND, "Frontend Developer"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 직무입니다.");
        }
    }

    @Nested
    @DisplayName("직무 삭제")
    class DeleteJob {

        private static final Long JOB_ID_FRONTEND = 99002L;

        @Test
        @DisplayName("직무를 삭제한다")
        void deleteJob_success() {
            Long deletedJobId = adminJobCommandService.deleteJob(JOB_ID_FRONTEND);
            assertThat(deletedJobId).isEqualTo(JOB_ID_FRONTEND);

            assertThatThrownBy(() -> adminJobCommandService.updateJob(JOB_ID_FRONTEND, "anything"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("직무를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 직무 ID이면 예외를 발생시킨다")
        void deleteJob_notFound_throwsException() {
            assertThatThrownBy(() -> adminJobCommandService.deleteJob(99999L))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("직무를 찾을 수 없습니다.");
        }
    }
}
