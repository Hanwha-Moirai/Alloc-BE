package com.moirai.alloc.admin.query.service;

import com.moirai.alloc.admin.query.dto.AdminTechStackListItem;
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
@Sql(scripts = "/sql/admin/tech_stack_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminTechStackQueryServiceTest {

    @Autowired
    private AdminTechStackQueryService service;

    @Test
    @DisplayName("기술 스택을 페이징 조회한다")
    void getTechStacks_success() {
        PageResponse<AdminTechStackListItem> res = service.getTechStacks(1, 10, null);

        assertThat(res.getContent()).isNotNull();
        assertThat(res.getContent().size()).isBetween(1, 10);

        // 전체 데이터 확인
        assertThat(res.getTotalElements()).isGreaterThan(0);

        // 페이지 파라미터 보정 확인
        assertThat(res.getCurrentPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(10);

    }
    @Test
    @DisplayName("검색어로 기술 스택을 필터링한다")
    void getTechStacks_withQuery() {
        PageResponse<AdminTechStackListItem> res = service.getTechStacks(1, 10, "DevOps");

        assertThat(res.getTotalElements()).isEqualTo(1);
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getTechId()).isEqualTo(99003L);
        assertThat(res.getContent().get(0).getTechName()).isEqualTo("DevOpsTool");
    }

    @Test
    @DisplayName("page와 size가 0 이하이면 최소값으로 보정된다")
    void pageAndSize_normalized() {
        PageResponse<AdminTechStackListItem> res = service.getTechStacks(0, 0, null);

        assertThat(res.getCurrentPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(1);
    }
}
