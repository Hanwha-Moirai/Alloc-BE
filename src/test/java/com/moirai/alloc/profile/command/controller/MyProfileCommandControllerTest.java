package com.moirai.alloc.profile.command.controller;

import com.moirai.alloc.auth.cookie.AuthCookieProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;

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
//@Sql(scripts = "/sql/profile/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = "/sql/profile/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class MyProfileCommandControllerTest {

    // setup.sql 기준 ID 값들
    private static final Long USER_ID_KMJ = 77001L;
    private static final Long EMPLOYEE_TECH_ID_JAVA = 1001L;
    private static final Long TECH_ID_JAVA = 1L;

    private static final Long JOB_ID_BACKEND = 1L;               // BackendDeveloper

    private static final LocalDate NEW_BIRTHDAY = LocalDate.of(1999, 1, 1);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthCookieProperties authCookieProperties;

    @Nested
    @DisplayName("기본 정보 수정 API")
    class UpdateMyProfileApi {

        @Test
        @DisplayName("인증되지 않은 사용자는 401을 받는다")
        void unauthorized() throws Exception {
            String requestJson = """
                    {
                        "email": "test@test.com"
                    }
                    """;

            mockMvc.perform(put("/api/users/me/profile")
                            .with(csrfToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("이메일/연락처/생년월일/직군을 수정한다")
        @WithUserDetails("kmj")
        void updateProfile_allFields_success() throws Exception {
            String requestJson = """
                    {
                      "email": "updated@alloc.co.kr",
                      "phone": "010-1111-2222",
                      "birthday": "1999-01-01",
                      "jobId": 1
                    }
                    """;

            mockMvc.perform(put("/api/users/me/profile")
                            .with(csrfToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID_KMJ))
                    .andExpect(jsonPath("$.data.userName").value("김명진"))
                    .andExpect(jsonPath("$.data.email").value("updated@alloc.co.kr"))
                    .andExpect(jsonPath("$.data.phone").value("010-1111-2222"))
                    .andExpect(jsonPath("$.data.birthday").value("1999-01-01"))
                    .andExpect(jsonPath("$.data.jobId").value(JOB_ID_BACKEND))
                    .andExpect(jsonPath("$.data.jobName").value("BackendDeveloper"))
                    .andDo(print());
        }

        @Test
        @DisplayName("변경 사항이 없으면 400을 받는다")
        @WithUserDetails("kmj")
        void noChanges_badRequest() throws Exception {
            String requestJson = "{}";

            mockMvc.perform(put("/api/users/me/profile")
                            .with(csrfToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("기술 스택 등록 API")
    class CreateTechStackApi {

        @Test
        @DisplayName("인증되지 않은 사용자는 401을 받는다")
        void unauthorized() throws Exception {
            String requestJson = """
                    {
                        "techId": 1,
                        "proficiency": "LV2"
                    }
                    """;

            mockMvc.perform(post("/api/users/me/tech-stacks")
                            .with(csrfToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("기술 스택을 등록한다")
        @WithUserDetails("nostack")
        void createTechStack_success() throws Exception {
            String requestJson = """
                    {
                        "techId": 1,
                        "proficiency": "LV3"
                    }
                    """;

            MvcResult result = mockMvc.perform(post("/api/users/me/tech-stacks")
                            .with(csrfToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andReturn();

            // 디버깅용 - 응답 확인
            System.out.println("=== Response Body ===");
            System.out.println(result.getResponse().getContentAsString());
            System.out.println("=== Response Status ===");
            System.out.println(result.getResponse().getStatus());
        }

        @Test
        @DisplayName("이미 등록된 기술이면 409를 받는다")
        @WithUserDetails("kmj")
        void duplicateTech_conflict() throws Exception {
            String requestJson = """
                    {
                        "techId": 1,
                        "proficiency": "LV3"
                    }
                    """;

            mockMvc.perform(post("/api/users/me/tech-stacks")
                            .with(csrfToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("기술 스택 숙련도 수정 API")
    class UpdateProficiencyApi {

        @Test
        @DisplayName("인증되지 않은 사용자는 401을 받는다")
        void unauthorized() throws Exception {
            String requestJson = """
                    {
                        "proficiency": "LV3"
                    }
                    """;

            mockMvc.perform(patch("/api/users/me/tech-stacks/" + EMPLOYEE_TECH_ID_JAVA + "/proficiency")
                            .with(csrfToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("숙련도를 수정한다")
        @WithUserDetails("kmj")
        void updateProficiency_success() throws Exception {
            String requestJson = """
                    {
                        "proficiency": "LV3"
                    }
                    """;

            mockMvc.perform(patch("/api/users/me/tech-stacks/" + EMPLOYEE_TECH_ID_JAVA + "/proficiency")
                            .with(csrfToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.employeeTechId").value(EMPLOYEE_TECH_ID_JAVA))
                    .andExpect(jsonPath("$.data.proficiency").value("LV3"))
                    .andDo(print());
        }

        @Test
        @DisplayName("다른 사용자의 기술 스택 수정 시 403을 받는다")
        @WithUserDetails("nostack")
        void forbidden() throws Exception {
            String requestJson = """
                    {
                        "proficiency": "LV3"
                    }
                    """;

            mockMvc.perform(patch("/api/users/me/tech-stacks/" + EMPLOYEE_TECH_ID_JAVA + "/proficiency")
                            .with(csrfToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("존재하지 않는 기술 스택이면 404를 받는다")
        @WithUserDetails("kmj")
        void notFound() throws Exception {
            String requestJson = """
                    {
                        "proficiency": "LV3"
                    }
                    """;

            mockMvc.perform(patch("/api/users/me/tech-stacks/999999/proficiency")
                            .with(csrfToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("기술 스택 삭제 API")
    class DeleteTechStackApi {

        @Test
        @DisplayName("인증되지 않은 사용자는 401을 받는다")
        void unauthorized() throws Exception {
            mockMvc.perform(delete("/api/users/me/tech-stacks/" + EMPLOYEE_TECH_ID_JAVA)
                            .with(csrfToken()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("기술 스택을 삭제한다")
        @WithUserDetails("kmj")
        void deleteTechStack_success() throws Exception {
            mockMvc.perform(delete("/api/users/me/tech-stacks/" + EMPLOYEE_TECH_ID_JAVA)
                            .with(csrfToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.employeeTechId").value(EMPLOYEE_TECH_ID_JAVA))
                    .andExpect(jsonPath("$.data.deleted").value(true))
                    .andDo(print());
        }

        @Test
        @DisplayName("다른 사용자의 기술 스택 삭제 시 403을 받는다")
        @WithUserDetails("nostack")
        void forbidden() throws Exception {
            mockMvc.perform(delete("/api/users/me/tech-stacks/" + EMPLOYEE_TECH_ID_JAVA)
                            .with(csrfToken()))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("존재하지 않는 기술 스택이면 404를 받는다")
        @WithUserDetails("kmj")
        void notFound() throws Exception {
            mockMvc.perform(delete("/api/users/me/tech-stacks/999999")
                            .with(csrfToken()))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    private RequestPostProcessor csrfToken() {
        return request -> {
            String token = "test-csrf-token";
            request.setCookies(new Cookie(authCookieProperties.getCsrfTokenName(), token));
            request.addHeader("X-CSRF-Token", token);
            return request;
        };
    }
}
