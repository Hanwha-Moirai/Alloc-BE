package com.moirai.alloc.admin.command.controller;

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
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        ServletTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
@Sql(scripts = "/sql/admin/user_setup.sql") // BEFORE_TEST_METHOD 디폴트
class AdminUserCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // user_setup.sql 기준
    private static final Long JOB_ID_BACKEND = 1L;
    private static final Long DEPT_ID_DEV = 1L;
    private static final Long TITLE_ID_JUNIOR = 1L;

    private static final Long EXIST_USER_ID = 77001L;   // 이미 존재하는 일반 유저
    private static final Long ADMIN_USER_ID = 99001L;   // admin 계정
    private static final String ADMIN_LOGIN_ID = "admin";
    private static final String NORMAL_LOGIN_ID = "kmj";  // 이미 존재하는 일반 유저

    @Nested
    @DisplayName("관리자 사용자 등록 API")
    class CreateUserApi {

        @Test
        @DisplayName("인증되지 않은 사용자는 401을 받는다")
        void unauthorized() throws Exception {
            String requestJson = """
                {
                  "loginId": "newuser",
                  "password": "password1234",
                  "userName": "신규사용자",
                  "birthday": "1999-01-01",
                  "email": "newuser@alloc.co.kr",
                  "phone": "010-1111-2222",
                  "auth": "USER",
                  "jobId": 1,
                  "deptId": 1,
                  "titleStandardId": 1,
                  "profileImg": null,
                  "employeeType": "FULL_TIME",
                  "hiringDate": "2025-01-01"
                }
                """;

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("관리자가 아닌 사용자는 403을 받는다")
        @WithUserDetails(NORMAL_LOGIN_ID)
        void forbidden_notAdmin() throws Exception {
            String requestJson = """
                {
                  "loginId": "newuser",
                  "password": "password1234",
                  "userName": "신규사용자",
                  "birthday": "1999-01-01",
                  "email": "newuser@alloc.co.kr",
                  "phone": "010-1111-2222",
                  "auth": "USER",
                  "jobId": 1,
                  "deptId": 1,
                  "titleStandardId": 1,
                  "employeeType": "FULL_TIME",
                  "hiringDate": "2025-01-01"
                }
                """;

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("관리자는 사용자를 등록한다")
        @WithUserDetails(ADMIN_LOGIN_ID)
        void createUser_success() throws Exception {
            String requestJson = """
                {
                  "loginId": "newuser",
                  "password": "password1234",
                  "userName": "신규사용자",
                  "birthday": "1999-01-01",
                  "email": "newuser@alloc.co.kr",
                  "phone": "010-1111-2222",
                  "auth": "USER",
                  "jobId": 1,
                  "deptId": 1,
                  "titleStandardId": 1,
                  "profileImg": "https://img.test/1.png",
                  "employeeType": "FULL_TIME",
                  "hiringDate": "2025-01-01"
                }
                """;

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").isNumber())
                    .andExpect(jsonPath("$.data.loginId").value("newuser"))
                    .andExpect(jsonPath("$.data.userName").value("신규사용자"))
                    .andExpect(jsonPath("$.data.email").value("newuser@alloc.co.kr"))
                    .andExpect(jsonPath("$.data.phone").value("010-1111-2222"))
                    .andExpect(jsonPath("$.data.birthday").value("1999-01-01"))
                    .andExpect(jsonPath("$.data.auth").value("USER"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE")) //기본값
                    .andExpect(jsonPath("$.data.jobId").value(JOB_ID_BACKEND))
                    .andExpect(jsonPath("$.data.deptId").value(DEPT_ID_DEV))
                    .andExpect(jsonPath("$.data.titleStandardId").value(TITLE_ID_JUNIOR))
                    .andDo(print());
        }

        @Test
        @DisplayName("loginId 중복이면 400(또는 409)을 받는다")
        @WithUserDetails(ADMIN_LOGIN_ID)
        void duplicateLoginId_fail() throws Exception {
            // setup.sql에 이미 존재하는 loginId를 사용
            String requestJson = """
                {
                  "loginId": "kmj",
                  "password": "password1234",
                  "userName": "중복사용자",
                  "birthday": "1999-01-01",
                  "email": "unique-email@alloc.co.kr",
                  "phone": "010-1111-2222",
                  "auth": "USER",
                  "jobId": 1,
                  "deptId": 1,
                  "titleStandardId": 1,
                  "employeeType": "FULL_TIME",
                  "hiringDate": "2025-01-01"
                }
                """;

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().is4xxClientError())
                    .andDo(print());
        }

        @Test
        @DisplayName("이메일 중복이면 400(또는 409)을 받는다")
        @WithUserDetails(ADMIN_LOGIN_ID)
        void duplicateEmail_fail() throws Exception {
            String requestJson = """
                {
                  "loginId": "unique-login",
                  "password": "password1234",
                  "userName": "중복이메일",
                  "birthday": "1999-01-01",
                  "email": "kmj@alloc.co.kr",
                  "phone": "010-1111-2222",
                  "auth": "USER",
                  "jobId": 1,
                  "deptId": 1,
                  "titleStandardId": 1,
                  "employeeType": "FULL_TIME",
                  "hiringDate": "2025-01-01"
                }
                """;

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().is4xxClientError())
                    .andDo(print());
        }

        @Test
        @DisplayName("유효성 검증 실패하면 400을 받는다")
        @WithUserDetails(ADMIN_LOGIN_ID)
        void validation_fail() throws Exception {
            // password 길이(min 10) 위반 + email 형식 위반
            String requestJson = """
                {
                  "loginId": "newuser2",
                  "password": "short",
                  "userName": "신규사용자",
                  "birthday": "1999-01-01",
                  "email": "not-an-email",
                  "phone": "010-1111-2222",
                  "auth": "USER",
                  "jobId": 1,
                  "deptId": 1,
                  "titleStandardId": 1,
                  "employeeType": "FULL_TIME",
                  "hiringDate": "2025-01-01"
                }
                """;

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("관리자 사용자 수정 API")
    class UpdateUserApi {

        @Test
        @DisplayName("인증되지 않은 사용자는 401을 받는다")
        void unauthorized() throws Exception {
            String requestJson = """
                { "userName": "수정이름" }
                """;

            mockMvc.perform(patch("/api/admin/users/" + EXIST_USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("관리자가 아닌 사용자는 403을 받는다")
        @WithUserDetails(NORMAL_LOGIN_ID)
        void forbidden_notAdmin() throws Exception {
            String requestJson = """
                { "userName": "수정이름" }
                """;

            mockMvc.perform(patch("/api/admin/users/" + EXIST_USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("관리자는 사용자 기본정보를 수정한다")
        @WithUserDetails(ADMIN_LOGIN_ID)
        void updateUser_basic_success() throws Exception {
            String requestJson = """
                {
                  "userName": "변경된이름",
                  "email": "changed@alloc.co.kr",
                  "phone": "010-9999-8888",
                  "birthday": "1998-12-31",
                  "profileImg": "https://img.test/changed.png"
                }
                """;

            mockMvc.perform(patch("/api/admin/users/" + EXIST_USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(EXIST_USER_ID))
                    .andExpect(jsonPath("$.data.userName").value("변경된이름"))
                    .andExpect(jsonPath("$.data.email").value("changed@alloc.co.kr"))
                    .andExpect(jsonPath("$.data.phone").value("010-9999-8888"))
                    .andExpect(jsonPath("$.data.birthday").value("1998-12-31"))
                    .andExpect(jsonPath("$.data.profileImg").value("https://img.test/changed.png"))
                    .andDo(print());
        }

        @Test
        @DisplayName("관리자는 HR 정보(직군/부서/직급/고용형태)를 수정한다")
        @WithUserDetails(ADMIN_LOGIN_ID)
        void updateUser_hr_success() throws Exception {
            String requestJson = """
                {
                  "jobId": 1,
                  "deptId": 1,
                  "titleStandardId": 1,
                  "employeeType": "FULL_TIME"
                }
                """;

            mockMvc.perform(patch("/api/admin/users/" + EXIST_USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(EXIST_USER_ID))
                    .andExpect(jsonPath("$.data.jobId").value(JOB_ID_BACKEND))
                    .andExpect(jsonPath("$.data.deptId").value(DEPT_ID_DEV))
                    .andExpect(jsonPath("$.data.titleStandardId").value(TITLE_ID_JUNIOR))
                    .andDo(print());
        }

        @Test
        @DisplayName("이메일이 다른 사용자와 중복이면 4xx를 받는다")
        @WithUserDetails(ADMIN_LOGIN_ID)
        void updateUser_duplicateEmail_fail() throws Exception {
            // setup.sql에 있는 다른 사용자 이메일을 넣어야 진짜 중복 테스트가 됨
            String requestJson = """
                {
                  "email": "admin@alloc.co.kr"
                }
                """;

            mockMvc.perform(patch("/api/admin/users/" + EXIST_USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().is4xxClientError())
                    .andDo(print());
        }

        @Test
        @DisplayName("존재하지 않는 userId면 404(또는 400)을 받는다")
        @WithUserDetails(ADMIN_LOGIN_ID)
        void updateUser_notFound() throws Exception {
            String requestJson = """
                { "userName": "수정이름" }
                """;

            mockMvc.perform(patch("/api/admin/users/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().is4xxClientError())
                    .andDo(print());
        }

        @Test
        @DisplayName("유효성 검증 실패(이메일 형식)면 400을 받는다")
        @WithUserDetails(ADMIN_LOGIN_ID)
        void updateUser_validation_fail() throws Exception {
            String requestJson = """
                { "email": "not-email" }
                """;

            mockMvc.perform(patch("/api/admin/users/" + EXIST_USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}