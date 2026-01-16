package com.moirai.alloc.management.controllerLayerTest;

import com.moirai.alloc.management.api.ProjectController;
import com.moirai.alloc.management.command.service.RegisterProject;
import com.moirai.alloc.management.query.service.GetProjectDetail;
import com.moirai.alloc.management.query.service.GetProjectList;
import com.moirai.alloc.management.query.service.GetProjectRegistrationView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetProjectList getProjectList;
    @MockBean
    private GetProjectDetail getProjectDetail;
    @MockBean
    private RegisterProject registerProject;
    @MockBean
    private GetProjectRegistrationView getProjectRegistrationView;

    private String validRegisterProjectJson() {
        return """
        {
          "name": "AI Project",
          "startDate": "2025-01-01",
          "endDate": "2025-06-30",
          "predictedCost": 1000000,
          "projectType": "NEW",
          "jobRequirements": [
            { "jobId": 1, "requiredCount": 2 }
          ],
          "techRequirements": [
            { "techId": 10, "techLevel": "LV1" }
          ]
        }
        """;
    }

    /**
     * [GET /api/projects]
     * - 인증된 사용자(USER)가 프로젝트 목록을 조회할 수 있는지 검증
     * - 정상 요청 시 HTTP 200 OK 반환
     * - Controller → Service(getProjectList) 호출 흐름 검증
     */
    @Test
    void getProjects_returnsOk_forAuthenticatedUser() throws Exception {
        when(getProjectList.getProjectList(anyLong()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/projects")
                        .with(authenticatedUser("USER")))
                .andExpect(status().isOk());
    }
    /**
     * [GET /api/projects]
     * - 인증되지 않은 사용자의 접근 차단 검증
     * - Spring Security 설정에 따라 403 Forbidden 반환 확인
     */
    @Test
    void getProjects_returnsForbidden_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isForbidden()); // ✅ 401 → 403
    }
    /**
     * [GET /api/projects]
     * - Security Context에 저장된 사용자 ID가
     *   Service Layer로 정확히 전달되는지 검증
     */
    @Test
    void getProjects_callsServiceWithPrincipalUserId() throws Exception {
        when(getProjectList.getProjectList(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/projects")
                        .with(authenticatedUser("USER")))
                .andExpect(status().isOk());

        verify(getProjectList).getProjectList(1L);
    }

    /**
     * [POST /api/projects]
     * - PM 권한 사용자가 프로젝트를 정상 등록할 수 있는지 검증
     * - RequestBody JSON → DTO 매핑 검증
     * - CSRF 토큰 포함 시 정상 처리 확인
     */
    @Test
    void registerProject_returnsOk_forPmUser() throws Exception {
        when(registerProject.registerProject(any(), anyLong()))
                .thenReturn(1L);

        mockMvc.perform(post("/api/projects")
                        .with(authenticatedUser("PM"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterProjectJson()))
                .andExpect(status().isOk());
    }
    /**
     * [POST /api/projects]
     * - PM 권한이 아닌 사용자(USER)의 접근 차단 검증
     * - Role 기반 접근 제어 → 403 Forbidden
     */
    @Test
    void registerProject_returnsForbidden_forNonPmUser() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .with(authenticatedUser("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterProjectJson()))
                .andExpect(status().isForbidden());
    }
    /**
     * [POST /api/projects]
     * - 인증되지 않은 사용자의 접근 차단 검증
     * - 인증 실패 시 403 Forbidden 반환 확인
     */
    @Test
    void registerProject_returnsForbidden_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterProjectJson()))
                .andExpect(status().isForbidden()); // ✅ 401 → 403
    }
    /**
     * [POST /api/projects]
     * - 요청 Body가 유효하지 않을 경우 Validation 동작 검증
     * - @Valid 실패 시 400 Bad Request 반환 확인
     */
    @Test
    void registerProject_returnsBadRequest_whenRequestBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .with(authenticatedUser("PM"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
