package com.moirai.alloc.management.command.controller;

import com.moirai.alloc.management.controller.ProjectAssignmentController;
import com.moirai.alloc.management.query.dto.selectedList.SelectedAssignmentMemberDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(ProjectAssignmentController.class)
@Import(TestSecurityConfig.class)
class AssignedMembersControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAssignedMembersPmSuccess() throws Exception {
        Long projectId = 1L;

        SelectedAssignmentMemberDTO dto =
                new SelectedAssignmentMemberDTO(
                        1L,
                        10L,
                        "김철수",
                        "Backend",
                        "Java",
                        500,
                        null,
                        null,
                        30,
                        30,
                        30,
                        90
                );

        given(getAssignedMembers.getMembers(projectId))
                .willReturn(List.of(dto));
        given(getAssignedStatus.getSummary(projectId))
                .willReturn(null);
        given(getAssignedStatus.getStatus(projectId))
                .willReturn(null);

        mockMvc.perform(
                        get("/api/projects/{projectId}/members", projectId)
                                .with(authenticatedUser("PM"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].userId").value(10L))
                .andExpect(jsonPath("$.members[0].userName").value("김철수"))
                .andExpect(jsonPath("$.members[0].jobName").value("Backend"));
    }

    @Test
    void getAssignedMembersUserSuccess() throws Exception {
        Long projectId = 1L;

        given(getAssignedMembers.getMembers(projectId))
                .willReturn(List.of());
        given(getAssignedStatus.getSummary(projectId))
                .willReturn(null);
        given(getAssignedStatus.getStatus(projectId))
                .willReturn(null);

        mockMvc.perform(
                        get("/api/projects/{projectId}/members", projectId)
                                .with(authenticatedUser("USER"))
                )
                .andExpect(status().isOk());
    }

    @Test
    void getAssignedMembersCallsServiceOnce() throws Exception {
        Long projectId = 1L;

        given(getAssignedMembers.getMembers(projectId))
                .willReturn(List.of());
        given(getAssignedStatus.getSummary(projectId))
                .willReturn(null);
        given(getAssignedStatus.getStatus(projectId))
                .willReturn(null);

        mockMvc.perform(
                        get("/api/projects/{projectId}/members", projectId)
                                .with(authenticatedUser("PM"))
                )
                .andExpect(status().isOk());

        verify(getAssignedMembers, times(1))
                .getMembers(projectId);
    }
}
