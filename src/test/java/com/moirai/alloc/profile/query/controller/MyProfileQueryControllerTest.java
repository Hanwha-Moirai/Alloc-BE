package com.moirai.alloc.profile.query.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        SqlScriptsTestExecutionListener.class
})
class MyProfileQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("인증되지 않은 사용자는 401 Unauthorized를 받는다")
    void getMyProfile_unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("상단바 프로필 요약 정보를 조회한다")
    @WithUserDetails("nostack")
    void getMySummary() throws Exception {
        mockMvc.perform(get("/api/users/me/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userName").value("김명진"))
                .andExpect(jsonPath("$.data.jobName").value("BackendDeveloper"))
                .andDo(print());
    }

    @Test
    @DisplayName("내 프로필 기본 정보를 조회한다")
    @WithUserDetails("kmj")
    void getMyProfile() throws Exception {
        mockMvc.perform(get("/api/users/me/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(77001))
                .andExpect(jsonPath("$.data.userName").value("김명진"))
                .andExpect(jsonPath("$.data.email").value("kmj@alloc.co.kr"))
                .andDo(print());
    }

    @Test
    @DisplayName("내 기술 스택 목록을 조회한다")
    @WithUserDetails("kmj")
    void getMyTechStacks() throws Exception {
        mockMvc.perform(get("/api/users/me/tech-stacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0].techName").value("Java"))
                .andDo(print());
    }

    @Test
    @DisplayName("기술 스택이 없는 경우 빈 목록을 반환한다")
    @WithUserDetails("nostack")
    void getMyTechStacks_noStacks() throws Exception {
        mockMvc.perform(get("/api/users/me/tech-stacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", empty()))
                .andDo(print());
    }

    @Test
    @DisplayName("내 프로젝트 히스토리를 조회한다")
    @WithUserDetails("kmj")
    void getMyProjectHistory() throws Exception {
        mockMvc.perform(get("/api/users/me/project-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].projectName").value("Project A"))
                .andDo(print());
    }

    @Test
    @DisplayName("프로젝트 히스토리가 없는 경우 빈 목록을 반환한다")
    @WithUserDetails("nohistory")
    void getMyProjectHistory_noHistory() throws Exception {
        mockMvc.perform(get("/api/users/me/project-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", empty()))
                .andDo(print());
    }
}