package com.moirai.alloc.admin.query.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
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
@Sql(scripts = "/sql/admin/job_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminJobQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Authentication adminAuth() {
        UserPrincipal principal = new UserPrincipal(
                90001L,
                "admin",
                "admin@test.com",
                "관리자",
                "ADMIN",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private Authentication userAuth() {
        UserPrincipal principal = new UserPrincipal(
                90002L,
                "user",
                "user@test.com",
                "사용자",
                "USER",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    @DisplayName("관리자는 직무 목록을 조회할 수 있다")
    void getJobs_asAdmin_success() throws Exception {
        mockMvc.perform(get("/api/admin/jobs")
                        .param("page", "1")
                        .param("size", "10")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andDo(print());
    }

    @Test
    @DisplayName("검색어로 직무 목록을 조회한다")
    void getJobs_withQuery() throws Exception {
        mockMvc.perform(get("/api/admin/jobs")
                        .param("page", "1")
                        .param("size", "10")
                        .param("q", "DevOps")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].jobName").value("DevOpsEngineer"))
                .andDo(print());
    }

    @Test
    @DisplayName("일반 사용자는 조회할 수 없다 (403)")
    void getJobs_asUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/jobs")
                        .param("page", "1")
                        .param("size", "10")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증되지 않으면 401")
    void getJobs_unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/jobs")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }
}
