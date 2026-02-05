package com.moirai.alloc.management.command.controller;

import com.moirai.alloc.management.controller.ProjectAssignmentController;
import com.moirai.alloc.management.query.dto.candidateList.CandidateScoreFilter;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentCandidatePageView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

    //점수 조정 파라미터 없이 호출하여도 정상적으로 반환되는지 검증
    @Test
    void getAssignmentCandidatePageReturnsOkWithoutFilter() throws Exception {
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
    void getAssignmentCandidatePageReturnsOkWithFilter() throws Exception {
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
    void assignCandidatesReturnsOkForPmUser() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments")
                        .with(authenticatedUser("PM"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "userIds": [1, 2, 3]
                            }
                            """))
                .andExpect(status().isOk());
    }


    /**
     * [POST /api/projects/{projectId}/assignments]
     * - PM 아닌 사용자 차단
     */
    @Test
    void assignCandidatesReturnsForbiddenForNonPmUser() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments")
                        .with(authenticatedUser("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

}
