package com.moirai.alloc.admin.query.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        ServletTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
@Sql(scripts = "/sql/admin/user_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminUserQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Authentication adminAuth() {
        UserPrincipal principal = new UserPrincipal(
                99001L,               // setup.sql의 admin user_id
                "admin",
                "admin@alloc.co.kr",
                "관리자",
                "ADMIN",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private Authentication userAuth() {
        UserPrincipal principal = new UserPrincipal(
                77001L,
                "kmj",
                "kmj@alloc.co.kr",
                "김명진",
                "USER",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Nested
    @DisplayName("GET /api/admin/users")
    class GetUsers {

        @Test
        @DisplayName("관리자는 사용자 목록을 조회할 수 있다")
        void getUsers_asAdmin_success() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .param("page", "1")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(5))
                    .andDo(print());
        }

        @Test
        @DisplayName("일반 사용자는 403을 받는다")
        void getUsers_asUser_forbidden() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth()))
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("미인증 사용자는 401을 받는다")
        void getUsers_unauthorized() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("role/status/q 파라미터가 적용된다")
        void getUsers_filters() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .param("page", "1")
                            .param("size", "10")
                            .param("role", "ADMIN")
                            .param("status", "ACTIVE")
                            .param("q", "관리자"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].auth").value("ADMIN"))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users/{userId}")
    class GetUserDetail {

        @Test
        @DisplayName("관리자는 사용자 상세 정보를 조회할 수 있다 (employee 포함)")
        void getUserDetail_asAdmin_success() throws Exception {
            mockMvc.perform(get("/api/admin/users/{userId}", 77001L)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))

                    // user 영역
                    .andExpect(jsonPath("$.data.userId").value(77001L))
                    .andExpect(jsonPath("$.data.loginId").value("kmj"))
                    .andExpect(jsonPath("$.data.userName").value("김명진"))
                    .andExpect(jsonPath("$.data.email").value("kmj@alloc.co.kr"))
                    .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                    .andExpect(jsonPath("$.data.auth").value("USER"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.profileImg").doesNotExist())

                    // employee 영역
                    .andExpect(jsonPath("$.data.jobId").value(1))
                    .andExpect(jsonPath("$.data.titleId").value(1))
                    .andExpect(jsonPath("$.data.deptId").value(1))
                    .andExpect(jsonPath("$.data.employeeType").value("FULL_TIME"))
                    .andExpect(jsonPath("$.data.hiringDate").value("2025-01-01"))

                    .andDo(print());
        }

        @Test
        @DisplayName("employee가 없는 사용자도 상세 조회된다")
        void getUserDetail_onlyUser_success() throws Exception {
            mockMvc.perform(get("/api/admin/users/{userId}", 88001L)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))

                    .andExpect(jsonPath("$.data.userId").value(88001L))
                    .andExpect(jsonPath("$.data.loginId").value("onlyuser"))

                    // employee 필드들은 null
                    .andExpect(jsonPath("$.data.jobId").isEmpty())
                    .andExpect(jsonPath("$.data.titleId").isEmpty())
                    .andExpect(jsonPath("$.data.deptId").isEmpty())
                    .andExpect(jsonPath("$.data.employeeType").isEmpty())
                    .andExpect(jsonPath("$.data.hiringDate").isEmpty())

                    .andDo(print());
        }

        @Test
        @DisplayName("일반 사용자는 403을 받는다")
        void getUserDetail_asUser_forbidden() throws Exception {
            mockMvc.perform(get("/api/admin/users/{userId}", 77001L)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 userId는 400(또는 404)을 반환한다")
        void getUserDetail_notFound() throws Exception {
            mockMvc.perform(get("/api/admin/users/{userId}", 999999L)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth())))
                    .andExpect(status().isBadRequest()); // IllegalArgumentException 기준
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users/meta")
    class GetUserMeta {

        @Test
        @DisplayName("관리자는 사용자 메타 데이터를 조회할 수 있다 +(직급/부서 포함)")
        void getUserMeta_asAdmin_success() throws Exception {
            mockMvc.perform(get("/api/admin/users/meta")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.employeeTypes").isArray())
                    .andExpect(jsonPath("$.data.auths").isArray())
                    .andExpect(jsonPath("$.data.statuses").isArray())

                    .andExpect(jsonPath("$.data.titles").isArray())
                    .andExpect(jsonPath("$.data.titles.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                    .andExpect(jsonPath("$.data.titles[0].id").exists())
                    .andExpect(jsonPath("$.data.titles[0].label").exists())

                    .andExpect(jsonPath("$.data.departments").isArray())
                    .andExpect(jsonPath("$.data.departments.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                    .andExpect(jsonPath("$.data.departments[0].id").exists())
                    .andExpect(jsonPath("$.data.departments[0].label").exists())
                    .andDo(print());
        }
    }
}
