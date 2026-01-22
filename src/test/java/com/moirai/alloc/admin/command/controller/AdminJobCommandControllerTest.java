package com.moirai.alloc.admin.command.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class AdminJobCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final Long JOB_ID_BACKEND = 99001L;
    private static final Long JOB_ID_FRONTEND = 99002L;

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
    @DisplayName("POST /api/admin/jobs")
    class CreateJob {

        @Test
        @DisplayName("관리자는 새로운 직무를 등록할 수 있다")
        void createJob_asAdmin_success() throws Exception {
            String requestJson = """
                    {
                      "jobName": "DevOps Engineer"
                    }
                    """;

            mockMvc.perform(post("/api/admin/jobs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber())
                    .andDo(print());
        }

        @Test
        @DisplayName("일반 사용자는 직무를 등록할 수 없다 (403 Forbidden)")
        void createJob_asUser_forbidden() throws Exception {
            String requestJson = """
                    {
                      "jobName": "Should Fail"
                    }
                    """;

            mockMvc.perform(post("/api/admin/jobs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 401 Unauthorized를 반환한다")
        void createJob_unauthorized() throws Exception {
            String requestJson = """
                    {
                      "jobName": "Should Fail"
                    }
                    """;

            mockMvc.perform(post("/api/admin/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("직무 이름이 없으면 400 Bad Request를 반환한다")
        void createJob_withBlankName_badRequest() throws Exception {
            String requestJson = """
                    {
                      "jobName": ""
                    }
                    """;

            mockMvc.perform(post("/api/admin/jobs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("공백만 다른 동일 직무는 중복으로 400을 반환한다")
        void createJob_duplicate_ignoringSpaces_badRequest() throws Exception {
            String requestJson = """
            { "jobName": "Backend Developer" }
            """;

            mockMvc.perform(post("/api/admin/jobs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/jobs/{job_id}")
    class UpdateJob {

        @Test
        @DisplayName("관리자는 직무 이름을 수정할 수 있다")
        void updateJob_asAdmin_success() throws Exception {
            String requestJson = """
                    {
                      "jobName": "Backend Engineer"
                    }
                    """;

            mockMvc.perform(patch("/api/admin/jobs/{job_id}", JOB_ID_BACKEND)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(JOB_ID_BACKEND))
                    .andDo(print());
        }

        @Test
        @DisplayName("일반 사용자는 직무를 수정할 수 없다 (403 Forbidden)")
        void updateJob_asUser_forbidden() throws Exception {
            String requestJson = """
                    {
                      "jobName": "Should Fail"
                    }
                    """;

            mockMvc.perform(patch("/api/admin/jobs/{job_id}", JOB_ID_BACKEND)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("존재하지 않는 직무는 404 Not Found를 반환한다")
        void updateJob_notFound() throws Exception {
            String requestJson = """
                    {
                      "jobName": "Non-existent"
                    }
                    """;

            mockMvc.perform(patch("/api/admin/jobs/{job_id}", 99999L)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("공백만 다른 동일 이름으로 수정하면 400을 반환한다")
        void updateJob_duplicate_ignoringSpaces_badRequest() throws Exception {
            String requestJson = """
            { "jobName": "Frontend Developer" }
            """;

            mockMvc.perform(patch("/api/admin/jobs/{job_id}", JOB_ID_BACKEND)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

    }

    @Nested
    @DisplayName("DELETE /api/admin/jobs/{job_id}")
    class DeleteJob {

        @Test
        @DisplayName("관리자는 직무를 삭제할 수 있다")
        void deleteJob_asAdmin_success() throws Exception {
            mockMvc.perform(delete("/api/admin/jobs/{job_id}", JOB_ID_FRONTEND)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(JOB_ID_FRONTEND))
                    .andDo(print());
        }

        @Test
        @DisplayName("일반 사용자는 직무를 삭제할 수 없다 (403 Forbidden)")
        void deleteJob_asUser_forbidden() throws Exception {
            mockMvc.perform(delete("/api/admin/jobs/{job_id}", JOB_ID_FRONTEND)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth())))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("존재하지 않는 직무는 404 Not Found를 반환한다")
        void deleteJob_notFound() throws Exception {
            mockMvc.perform(delete("/api/admin/jobs/{job_id}", 99999L)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth())))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }
}
