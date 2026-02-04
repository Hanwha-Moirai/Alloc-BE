package com.moirai.alloc.admin.command.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jakarta.servlet.http.Cookie;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        ServletTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
@Sql(scripts = "/sql/admin/tech_stack_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminTechStackCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String CSRF_TOKEN = "test-csrf-token";

    private static RequestPostProcessor withCsrf() {
        return request -> {
            request.addHeader("X-CSRF-Token", CSRF_TOKEN);
            request.setCookies(new Cookie("csrfToken", CSRF_TOKEN));
            return request;
        };
    }

    @Test
    @DisplayName("관리자는 기술 스택을 등록할 수 있다")
    void createTechStack_returnsId() throws Exception {
        String body = """
                {
                  "techName": "Go"
                }
                """;

        mockMvc.perform(post("/api/admin/tech-stacks")
                        .with(withCsrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("관리자는 기술 스택을 수정할 수 있다")
    void updateTechStack_returnsId() throws Exception {
        String body = """
                {
                  "techName": "Kotlin"
                }
                """;

        mockMvc.perform(patch("/api/admin/tech-stacks/{stackId}", 99001)
                        .with(withCsrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(99001));
    }

    @Test
    @DisplayName("관리자는 기술 스택을 삭제할 수 있다")
    void deleteTechStack_returnsId() throws Exception {
        mockMvc.perform(delete("/api/admin/tech-stacks/{stackId}", 99002)
                        .with(withCsrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(99002));
    }

    @Test
    @DisplayName("일반 사용자는 기술 스택을 등록할 수 없다 (403)")
    void createTechStack_forbiddenWhenUserIsNotAdmin() throws Exception {
        String body = """
                {
                  "techName": "Rust"
                }
                """;

        mockMvc.perform(post("/api/admin/tech-stacks")
                        .with(withCsrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

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
}
