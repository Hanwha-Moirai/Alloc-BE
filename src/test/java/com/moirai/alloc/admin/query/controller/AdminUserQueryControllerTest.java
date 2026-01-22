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
    @DisplayName("GET /api/admin/users/meta")
    class GetUserMeta {

        @Test
        @DisplayName("관리자는 사용자 메타 데이터를 조회할 수 있다")
        void getUserMeta_asAdmin_success() throws Exception {
            mockMvc.perform(get("/api/admin/users/meta")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.employeeTypes").isArray())
                    .andExpect(jsonPath("$.data.auths").isArray())
                    .andExpect(jsonPath("$.data.statuses").isArray())
                    .andDo(print());
        }
    }
}
