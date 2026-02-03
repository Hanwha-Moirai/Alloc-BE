package com.moirai.alloc.admin.query.service;

import com.moirai.alloc.admin.query.dto.AdminUserDetailResponse;
import com.moirai.alloc.admin.query.dto.AdminUserListItem;
import com.moirai.alloc.admin.query.dto.AdminUserMetaResponse;
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

import java.time.LocalDate;

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
        PageResponse<AdminUserListItem> res = service.getUsers(0, 10, null, null, null);

        // setup 기준: users 5명
        assertThat(res.getTotalElements()).isGreaterThanOrEqualTo(5);
        assertThat(res.getContent()).isNotEmpty();
        assertThat(res.getCurrentPage()).isEqualTo(0);
        assertThat(res.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("role=ADMIN 필터링이 적용된다")
    void getUsers_filter_role() {
        PageResponse<AdminUserListItem> res = service.getUsers(0, 10, null, "ADMIN", null);

        assertThat(res.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(res.getContent())
                .extracting(AdminUserListItem::getAuth)
                .containsOnly("ADMIN");
    }

    @Test
    @DisplayName("status=SUSPENDED 필터링이 적용된다")
    void getUsers_filter_status() {
        PageResponse<AdminUserListItem> res = service.getUsers(0, 10, null, null, "SUSPENDED");

        assertThat(res.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(res.getContent())
                .extracting(AdminUserListItem::getStatus)
                .containsOnly("SUSPENDED");
    }

    @Test
    @DisplayName("q 검색이 user_name/email/login_id/job/dept/title에 대해 동작한다")
    void getUsers_filter_query() {
        // 'BackendDeveloper'는 job_name
        PageResponse<AdminUserListItem> res = service.getUsers(0, 10, "Backend", null, null);

        // employee가 있는 4명은 job이 있으므로 매칭됨(onlyuser는 employee 없어서 jobName null)
        assertThat(res.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(res.getContent())
                .allMatch(it -> it.getJobName() != null && it.getJobName().contains("Backend"));
    }

    @Test
    @DisplayName("employee가 없는 사용자도 조회된다(LEFT JOIN)")
    void getUsers_include_onlyUser() {
        PageResponse<AdminUserListItem> res = service.getUsers(0, 10, "onlyuser", null, null);

        assertThat(res.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(res.getContent())
                .extracting(AdminUserListItem::getUserId)
                .contains(88001L);
        AdminUserListItem item = res.getContent().stream()
                .filter(it -> it.getUserId().equals(88001L))
                .findFirst()
                .orElseThrow();
        assertThat(item.getJobName()).isNull();
        assertThat(item.getDeptName()).isNull();
        assertThat(item.getTitleName()).isNull();
    }

    @Test
    @DisplayName("page/size가 0 이하이면 최소값으로 보정된다")
    void pageSize_normalized() {
        PageResponse<AdminUserListItem> res = service.getUsers(0, 0, null, null, null);

        assertThat(res.getCurrentPage()).isEqualTo(0);
        assertThat(res.getSize()).isEqualTo(1);
        assertThat(res.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("사용자 상세 조회 (employee 포함)")
    void getUserDetail_success() {
        AdminUserDetailResponse res = service.getUserDetail(77001L);

        assertThat(res.getUserId()).isEqualTo(77001L);
        assertThat(res.getLoginId()).isEqualTo("kmj");
        assertThat(res.getUserName()).isEqualTo("김명진");
        assertThat(res.getEmail()).isEqualTo("kmj@alloc.co.kr");
        assertThat(res.getPhone()).isEqualTo("010-1234-5678");
        assertThat(res.getAuth()).isEqualTo("USER");
        assertThat(res.getStatus()).isEqualTo("ACTIVE");

        // employee
        assertThat(res.getJobId()).isEqualTo(1L);
        assertThat(res.getTitleId()).isEqualTo(1L);
        assertThat(res.getDeptId()).isEqualTo(1L);
        assertThat(res.getEmployeeType()).isEqualTo("FULL_TIME");
        assertThat(res.getHiringDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("employee가 없는 사용자 상세 조회")
    void getUserDetail_onlyUser() {
        AdminUserDetailResponse res = service.getUserDetail(88001L);

        assertThat(res.getUserId()).isEqualTo(88001L);
        assertThat(res.getLoginId()).isEqualTo("onlyuser");

        assertThat(res.getJobId()).isNull();
        assertThat(res.getTitleId()).isNull();
        assertThat(res.getDeptId()).isNull();
        assertThat(res.getEmployeeType()).isNull();
        assertThat(res.getHiringDate()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
    void getUserDetail_notFound() {
        assertThatThrownBy(() -> service.getUserDetail(999999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 메타 조회 시 직급/부서/근무형태/권한/계정상태 옵션이 포함된다")
    void getUserMeta_includes_titles_and_departments() {
        AdminUserMetaResponse res = service.getUserMeta();

        assertThat(res.getEmployeeTypes()).isNotNull();
        assertThat(res.getAuths()).isNotNull();
        assertThat(res.getStatuses()).isNotNull();

        assertThat(res.getTitles()).isNotNull().isNotEmpty();
        assertThat(res.getDepartments()).isNotNull().isNotEmpty();

        assertThat(res.getTitles().get(0).getId()).isNotNull();
        assertThat(res.getTitles().get(0).getLabel()).isNotBlank();

        assertThat(res.getDepartments().get(0).getId()).isNotNull();
        assertThat(res.getDepartments().get(0).getLabel()).isNotBlank();
    }
}
