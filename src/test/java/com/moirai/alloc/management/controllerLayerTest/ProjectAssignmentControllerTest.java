package com.moirai.alloc.management.controllerLayerTest;

import com.moirai.alloc.management.api.ProjectAssignmentController;
import com.moirai.alloc.management.command.service.SelectAdditionalAssignmentCandidates;
import com.moirai.alloc.management.command.service.SelectAssignmentCandidates;
import com.moirai.alloc.management.query.dto.candidateList.CandidateScoreFilter;
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

@WebMvcTest(ProjectAssignmentController.class)
class ProjectAssignmentControllerTest extends ControllerTestSupport {

    @Autowired
    MockMvc mockMvc;

    @MockBean private GetAssignmentCandidates getAssignmentCandidates;
    @MockBean private GetAssignmentMembers getAssignmentMembers;
    @MockBean private GetAssignmentStatus getAssignmentStatus;
    @MockBean private SelectAssignmentCandidates selectAssignmentCandidates;
    @MockBean private SelectAdditionalAssignmentCandidates selectAdditionalAssignmentCandidates;
    @MockBean private GetAssignedMembers getAssignedMembers;

    /**
     * [GET /api/projects/{projectId}/assign]
     * - 파라미터 없이 후보 조회
     */
    @Test
    void getAssignmentCandidatePage_returnsOk_withoutFilter() throws Exception {
        when(getAssignmentCandidates.getAssignmentCandidates(
                anyLong(),
                org.mockito.ArgumentMatchers.isNull()
        )).thenReturn(new AssignmentCandidatePageView(List.of(), List.of()));

        mockMvc.perform(get("/api/projects/1/assign")
                        .with(authenticatedUser("USER")))
                .andExpect(status().isOk());
    }

    /**
     * [GET /api/projects/{projectId}/assign]
     * - 파라미터 포함 후보 조회
     */
    @Test
    void getAssignmentCandidatePage_returnsOk_withFilter() throws Exception {
        when(getAssignmentCandidates.getAssignmentCandidates(
                anyLong(),
                org.mockito.ArgumentMatchers.any(CandidateScoreFilter.class)
        )).thenReturn(new AssignmentCandidatePageView(List.of(), List.of()));

        mockMvc.perform(get("/api/projects/1/assign")
                        .param("skill", "80")
                        .param("experience", "90")
                        .param("availability", "70")
                        .with(authenticatedUser("USER")))
                .andExpect(status().isOk());
    }

    /**
     * [POST /api/projects/{projectId}/assignments]
     * - PM 권한 사용자 가능
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
     * - PM 아닌 사용자 차단
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
     * - PM 아닌 사용자 차단
     */
    @Test
    void addMoreCandidates_returnsForbidden_forNonPmUser() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments/additional")
                        .with(authenticatedUser("USER")))
                .andExpect(status().isForbidden());
    }
}
