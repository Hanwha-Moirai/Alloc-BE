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
    @DisplayName("0-based 페이지: page=0,size=10이면 1페이지(첫 10개)가 조회된다")
    void getJobs_page0_size10() {
        PageResponse<AdminJobListItem> res = service.getJobs(0, 10, null);

        assertThat(res.getContent()).hasSize(10);
        assertThat(res.getTotalElements()).isGreaterThanOrEqualTo(31);
        assertThat(res.getTotalPages()).isGreaterThanOrEqualTo(4);
        assertThat(res.getCurrentPage()).isEqualTo(0);
        assertThat(res.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("0-based 페이지: page=1,size=10이면 2페이지(11~20번째)가 조회된다")
    void getJobs_page1_size10() {
        PageResponse<AdminJobListItem> res = service.getJobs(1, 10, null);

        assertThat(res.getContent()).hasSize(10);
        assertThat(res.getCurrentPage()).isEqualTo(1);

        assertThat(res.getContent()).hasSize(10);
    }

    @Test
    @DisplayName("page=0과 page=1의 content가 동일하면 안 된다 (중복 페이지 방지 검증)")
    void pages_mustBeDifferent() {
        PageResponse<AdminJobListItem> page0 = service.getJobs(0, 10, null);
        PageResponse<AdminJobListItem> page1 = service.getJobs(1, 10, null);

        assertThat(page0.getContent()).isNotEqualTo(page1.getContent());
        assertThat(page0.getContent().get(0).getJobName()).isNotEqualTo(page1.getContent().get(0).getJobName());
    }

    @Test
    @DisplayName("검색어로 직무 목록을 필터링한다")
    void getJobs_withQuery_backend() {
        PageResponse<AdminJobListItem> res = service.getJobs(0, 10, "Backend");

        assertThat(res.getContent()).isNotEmpty();
        assertThat(res.getContent())
                .extracting(AdminJobListItem::getJobName)
                .contains("BackendDeveloper");
    }

    @Test
    @DisplayName("page<0 또는 size<=0이면 최소값으로 보정된다")
    void pageAndSize_normalized() {
        PageResponse<AdminJobListItem> res = service.getJobs(-1, 0, null);

        assertThat(res.getCurrentPage()).isEqualTo(0); // 0-based 최소
        assertThat(res.getSize()).isEqualTo(1);        // size 최소
    }
}

