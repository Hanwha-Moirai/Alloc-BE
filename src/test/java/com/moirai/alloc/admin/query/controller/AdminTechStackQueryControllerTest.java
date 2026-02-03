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
@Sql(scripts = "/sql/admin/tech_stack_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminTechStackQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Authentication adminAuth() {
        UserPrincipal principal = new UserPrincipal(
                90001L,
                "admin_90001",
                "admin90001@example.com",
                "Admin User",
                "ADMIN",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private Authentication userAuth() {
        UserPrincipal principal = new UserPrincipal(
                90002L,
                "user_90002",
                "user90002@example.com",
                "User",
                "USER",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Nested
    @DisplayName("GET /api/admin/tech-stacks")
    class GetTechStacks {

        @Test
        @DisplayName("관리자는 기술스택을 페이징 조회할 수 있다")
        void getTechStacks_asAdmin_success() throws Exception {
            mockMvc.perform(get("/api/admin/tech-stacks")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .param("page", "1")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(4)))
                    .andDo(print());
        }

        @Test
        @DisplayName("관리자는 검색어로 기술스택을 필터링할 수 있다")
        void getTechStacks_asAdmin_withQuery() throws Exception {
            mockMvc.perform(get("/api/admin/tech-stacks")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .param("page", "0")
                            .param("size", "10")
                            .param("q", "Docker")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].techId").value(99003))
                    .andExpect(jsonPath("$.data.content[0].techName").value("Docker"))
                    .andDo(print());
        }

        @Test
        @DisplayName("일반 사용자는 403 Forbidden")
        void getTechStacks_asUser_forbidden() throws Exception {
            mockMvc.perform(get("/api/admin/tech-stacks")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth()))
                            .param("page", "1")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("인증되지 않으면 401 Unauthorized")
        void getTechStacks_unauthorized() throws Exception {
            mockMvc.perform(get("/api/admin/tech-stacks")
                            .param("page", "1")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }
}
