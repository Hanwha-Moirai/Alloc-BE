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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jakarta.servlet.http.Cookie;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@Sql(scripts = "/sql/admin/title_standard_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminTitleStandardCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // title_setup.sql 기준 ID
    private static final Long TITLE_ID_JUNIOR = 99001L;
    private static final Long TITLE_ID_SENIOR = 99002L;
    private static final String CSRF_TOKEN = "test-csrf-token";

    private static RequestPostProcessor withCsrf() {
        return request -> {
            request.addHeader("X-CSRF-Token", CSRF_TOKEN);
            request.setCookies(new Cookie("csrfToken", CSRF_TOKEN));
            return request;
        };
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

    @Nested
    @DisplayName("POST /api/admin/titles")
    class CreateTitle {

        @Test
        @DisplayName("관리자는 직급을 등록할 수 있다")
        void createTitle_asAdmin_success() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Lead",
                      "monthlyCost": 3000000
                    }
                    """;

            mockMvc.perform(post("/api/admin/titles")
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber())
                    .andDo(print());
        }

        @Test
        @DisplayName("일반 사용자는 직급을 등록할 수 없다 (403)")
        void createTitle_asUser_forbidden() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Lead",
                      "monthlyCost": 3000000
                    }
                    """;

            mockMvc.perform(post("/api/admin/titles")
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 401을 받는다")
        void createTitle_unauthorized() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Lead",
                      "monthlyCost": 3000000
                    }
                    """;

            mockMvc.perform(post("/api/admin/titles")
                            .with(withCsrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("titleName이 blank면 400을 받는다")
        void createTitle_blankName_badRequest() throws Exception {
            String requestJson = """
                    {
                      "titleName": "",
                      "monthlyCost": 3000000
                    }
                    """;

            mockMvc.perform(post("/api/admin/titles")
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("monthlyCost가 null이면 400을 받는다")
        void createTitle_nullMonthlyCost_badRequest() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Lead",
                      "monthlyCost": null
                    }
                    """;

            mockMvc.perform(post("/api/admin/titles")
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("monthlyCost가 음수면 400을 받는다")
        void createTitle_negativeMonthlyCost_badRequest() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Lead",
                      "monthlyCost": -1
                    }
                    """;

            mockMvc.perform(post("/api/admin/titles")
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/titles/{title_id}")
    class UpdateTitle {

        @Test
        @DisplayName("관리자는 직급을 수정할 수 있다")
        void updateTitle_asAdmin_success() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Junior",
                      "monthlyCost": 1500000
                    }
                    """;

            mockMvc.perform(patch("/api/admin/titles/{title_id}", TITLE_ID_JUNIOR)
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(TITLE_ID_JUNIOR))
                    .andDo(print());
        }

        @Test
        @DisplayName("일반 사용자는 직급을 수정할 수 없다 (403)")
        void updateTitle_asUser_forbidden() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Junior",
                      "monthlyCost": 1500000
                    }
                    """;

            mockMvc.perform(patch("/api/admin/titles/{title_id}", TITLE_ID_JUNIOR)
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(userAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 401을 받는다")
        void updateTitle_unauthorized() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Junior",
                      "monthlyCost": 1500000
                    }
                    """;

            mockMvc.perform(patch("/api/admin/titles/{title_id}", TITLE_ID_JUNIOR)
                            .with(withCsrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("존재하지 않는 titleId는 404를 받는다")
        void updateTitle_notFound() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Ghost",
                      "monthlyCost": 0
                    }
                    """;

            mockMvc.perform(patch("/api/admin/titles/{title_id}", 99999L)
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("titleName이 blank면 400을 받는다")
        void updateTitle_blankName_badRequest() throws Exception {
            String requestJson = """
                    {
                      "titleName": "",
                      "monthlyCost": 1000
                    }
                    """;

            mockMvc.perform(patch("/api/admin/titles/{title_id}", TITLE_ID_JUNIOR)
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("monthlyCost가 null이면 400을 받는다")
        void updateTitle_nullMonthlyCost_badRequest() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Junior",
                      "monthlyCost": null
                    }
                    """;

            mockMvc.perform(patch("/api/admin/titles/{title_id}", TITLE_ID_JUNIOR)
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("monthlyCost가 음수면 400을 받는다")
        void updateTitle_negativeMonthlyCost_badRequest() throws Exception {
            String requestJson = """
                    {
                      "titleName": "Junior",
                      "monthlyCost": -10
                    }
                    """;

            mockMvc.perform(patch("/api/admin/titles/{title_id}", TITLE_ID_JUNIOR)
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("중복 직급명으로 수정 시 400을 받는다 (서비스 정책 기준)")
        void updateTitle_duplicate_badRequest() throws Exception {
            // Junior를 Senior로 바꾸려고 하면 existsByTitleNameIgnoreCase("Senior")가 true가 되고,
            // 현재 엔티티 titleName != "Senior" 이므로 duplicated=true -> IllegalArgumentException -> 400
            String requestJson = """
                    {
                      "titleName": "Senior",
                      "monthlyCost": 999
                    }
                    """;

            mockMvc.perform(patch("/api/admin/titles/{title_id}", TITLE_ID_JUNIOR)
                            .with(withCsrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}
