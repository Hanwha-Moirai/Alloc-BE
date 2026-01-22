package com.moirai.alloc.admin.query.service;

import com.moirai.alloc.admin.query.dto.AdminJobListItem;
import com.moirai.alloc.common.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
@Sql(scripts = "/sql/admin/job_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminJobQueryServiceTest {

    @Autowired
    private AdminJobQueryService service;

    @Test
    @DisplayName("직무 목록을 페이징 조회한다")
    void getJobs_success() {
        PageResponse<AdminJobListItem> res = service.getJobs(1, 10, null);

        assertThat(res.getContent()).hasSize(3);
        assertThat(res.getTotalElements()).isEqualTo(3);
        assertThat(res.getCurrentPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("검색어로 직무 목록을 필터링한다")
    void getJobs_withQuery() {
        PageResponse<AdminJobListItem> res = service.getJobs(1, 10, "Backend");

        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getJobName()).isEqualTo("Backend Developer");
    }

    @Test
    @DisplayName("page와 size가 0 이하이면 최소값으로 보정된다")
    void pageAndSize_normalized() {
        PageResponse<AdminJobListItem> res = service.getJobs(0, 0, null);

        assertThat(res.getCurrentPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(1);
    }

}
