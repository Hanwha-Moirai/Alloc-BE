package com.moirai.alloc.admin.query.service;

import com.moirai.alloc.admin.query.dto.AdminUserListItem;
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
@Sql(scripts = "/sql/admin/user_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminUserQueryServiceTest {

    @Autowired
    private AdminUserQueryService service;

    @Test
    @DisplayName("사용자 목록을 페이징 조회한다")
    void getUsers_success() {
        PageResponse<AdminUserListItem> res = service.getUsers(1, 10, null, null, null);

        // setup 기준: users 5명
        assertThat(res.getTotalElements()).isEqualTo(5);
        assertThat(res.getContent()).hasSize(5);
        assertThat(res.getCurrentPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("role=ADMIN 필터링이 적용된다")
    void getUsers_filter_role() {
        PageResponse<AdminUserListItem> res = service.getUsers(1, 10, null, "ADMIN", null);

        assertThat(res.getTotalElements()).isEqualTo(1);
        assertThat(res.getContent())
                .extracting(AdminUserListItem::getAuth)
                .containsOnly("ADMIN");
    }

    @Test
    @DisplayName("status=SUSPENDED 필터링이 적용된다")
    void getUsers_filter_status() {
        PageResponse<AdminUserListItem> res = service.getUsers(1, 10, null, null, "SUSPENDED");

        assertThat(res.getTotalElements()).isEqualTo(1);
        assertThat(res.getContent().get(0).getUserName()).isEqualTo("PM사용자");
        assertThat(res.getContent().get(0).getStatus()).isEqualTo("SUSPENDED");
    }

    @Test
    @DisplayName("q 검색이 user_name/email/login_id/job/dept/title에 대해 동작한다")
    void getUsers_filter_query() {
        // 'BackendDeveloper'는 job_name
        PageResponse<AdminUserListItem> res = service.getUsers(1, 10, "Backend", null, null);

        // employee가 있는 4명은 job이 있으므로 매칭됨(onlyuser는 employee 없어서 jobName null)
        assertThat(res.getTotalElements()).isEqualTo(4);
        assertThat(res.getContent())
                .allMatch(it -> it.getJobName() != null);
    }

    @Test
    @DisplayName("employee가 없는 사용자도 조회된다(LEFT JOIN)")
    void getUsers_include_onlyUser() {
        PageResponse<AdminUserListItem> res = service.getUsers(1, 10, "onlyuser", null, null);

        assertThat(res.getTotalElements()).isEqualTo(1);
        AdminUserListItem item = res.getContent().get(0);
        assertThat(item.getUserId()).isEqualTo(88001L);
        assertThat(item.getJobName()).isNull();   // employee가 없으니 null이어야 정상
        assertThat(item.getDeptName()).isNull();
        assertThat(item.getTitleName()).isNull();
    }

    @Test
    @DisplayName("page/size가 0 이하이면 최소값으로 보정된다")
    void pageSize_normalized() {
        PageResponse<AdminUserListItem> res = service.getUsers(0, 0, null, null, null);

        assertThat(res.getCurrentPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(1);
        assertThat(res.getContent()).hasSize(1);
    }
}
