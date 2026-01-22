package com.moirai.alloc.admin.query.service;

import com.moirai.alloc.admin.query.dto.AdminTitleStandardListItem;
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
@Sql(scripts = "/sql/admin/title_standard_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminTitleStandardQueryServiceTest {

    @Autowired
    private AdminTitleStandardQueryService service;

    @Test
    @DisplayName("직급 목록을 페이징 조회한다")
    void getTitleStandard_success() {
        PageResponse<AdminTitleStandardListItem> res = service.getTitleStandard(1, 10, null);

        assertThat(res).isNotNull();
        assertThat(res.getContent()).isNotNull();

        assertThat(res.getContent().size()).isBetween(1, 10);
        assertThat(res.getTotalElements()).isGreaterThan(0);

        assertThat(res.getCurrentPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("검색어로 직급 목록을 필터링한다")
    void getTitleStandard_withQuery() {
        PageResponse<AdminTitleStandardListItem> res = service.getTitleStandard(1, 10, "사원");

        assertThat(res.getTotalElements()).isEqualTo(1);
        assertThat(res.getContent())
                .extracting(AdminTitleStandardListItem::getTitleName)
                .allMatch(name -> name.startsWith("사원"));
    }

    @Test
    @DisplayName("page와 size가 0 이하이면 최소값으로 보정된다")
    void pageAndSize_normalized() {
        PageResponse<AdminTitleStandardListItem> res = service.getTitleStandard(0, 0, null);

        assertThat(res.getCurrentPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(1);
        assertThat(res.getTotalElements()).isEqualTo(4);
        assertThat(res.getContent().size()).isEqualTo(1);
    }
}
