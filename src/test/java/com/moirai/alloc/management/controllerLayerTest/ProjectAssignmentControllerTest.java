package com.moirai.alloc.management.controllerLayerTest;

import com.moirai.alloc.management.api.ProjectAssignmentController;
import com.moirai.alloc.management.command.service.SelectAdditionalAssignmentCandidates;
import com.moirai.alloc.management.command.service.SelectAssignmentCandidates;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentCandidatePageView;
import com.moirai.alloc.management.query.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ProjectAssignmentController.class)
class ProjectAssignmentControllerTest extends ControllerTestSupport {

    @Autowired
    MockMvc mockMvc;

    @MockBean private GetAssignmentCandidates getAssignmentCandidates;
    @MockBean private GetAssignmentMembers getAssignmentMembers;
    @MockBean private GetAssignmentStatus getAssignmentStatus;
    @MockBean private SelectAssignmentCandidates selectAssignmentCandidates;
    @MockBean private SelectAdditionalAssignmentCandidates selectAdditionalAssignmentCandidates;
    @MockBean private GetProjectList getProjectList;
    @MockBean private GetAssignedMembers getAssignedMembers;


    /**
     * [GET /api/projects/{projectId}/assign]
     * - 인증된 사용자가 인력 배치 후보 조회 화면 데이터를 조회할 수 있는지 검증
     * - Controller → Query Service 호출 및 응답 매핑 검증
     */
    @Test
    void getAssignmentCandidatePage_returnsOk_forAuthenticatedUser() throws Exception {
        when(getAssignmentCandidates.getAssignmentCandidates(anyLong()))
                .thenReturn(new AssignmentCandidatePageView(List.of(), List.of()));


        mockMvc.perform(get("/api/projects/1/assign")
                        .with(authenticatedUser("USER")))
                .andExpect(status().isOk());
    }

    /**
     * [POST /api/projects/{projectId}/assignments]
     * - PM 권한 사용자가 인력 배치를 저장할 수 있는지 검증
     * - RequestBody 매핑 및 권한 검증
     */
    @Test
    void assignCandidates_returnsOk_forPmUser() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments")
                        .with(authenticatedUser("PM"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": 1,
                                  "assignments": []
                                }
                                """))
                .andExpect(status().isOk());
    }
    /**
     * [POST /api/projects/{projectId}/assignments]
     * - PM 권한이 없는 사용자의 인력 배치 시도 차단 검증
     * - Role 기반 접근 제어 → 403 Forbidden
     */
    @Test
    void assignCandidates_returnsForbidden_forNonPmUser() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments")
                        .with(authenticatedUser("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
    /**
     * [POST /api/projects/{projectId}/assignments/additional]
     * - 추가 인력 선발 기능에 대해
     *   PM 권한이 없는 사용자 접근 차단 검증
     */
    @Test
    void addMoreCandidates_returnsForbidden_forNonPmUser() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments/additional")
                        .with(authenticatedUser("USER")))
                .andExpect(status().isForbidden());
    }
    /**
     * [GET /api/projects]
     * - Service Layer에서 RuntimeException 발생 시
     *   Global Exception Handler가 적용되는지 검증
     * - HTTP 500 반환
     * - 공통 에러 응답 포맷(success=false, errorCode) 검증
     */
    @Test
    void getProjects_returnsInternalServerError_whenServiceThrowsException() throws Exception {
        when(getProjectList.getProjectList(anyLong()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/projects")
                        .with(authenticatedUser("USER")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"));
    }

}
