package com.moirai.alloc.user.command.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@Sql(scripts = "/sql/profile/setup.sql")
class UserMeControllerTest {

    private static final Long USER_ID_KMJ = 77001L;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("인증되지 않은 사용자는 401을 받는다")
    void unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 정보 조회에 성공한다")
    @WithUserDetails("kmj")
    void me_success() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(USER_ID_KMJ))
                .andExpect(jsonPath("$.data.loginId").value("kmj"))
                .andExpect(jsonPath("$.data.email").value("kmj@alloc.co.kr"))
                .andExpect(jsonPath("$.data.name").value("김명진"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }
}
