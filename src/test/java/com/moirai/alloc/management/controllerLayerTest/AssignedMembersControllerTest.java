package com.moirai.alloc.management.controllerLayerTest;

import com.moirai.alloc.management.controller.ProjectAssignmentController;
import com.moirai.alloc.management.command.service.SelectAdditionalAssignmentCandidates;
import com.moirai.alloc.management.command.service.SelectAssignmentCandidates;
import com.moirai.alloc.management.query.dto.selectedWorker.AssignedMemberDTO;
import com.moirai.alloc.management.query.service.GetAssignedMembers;
import com.moirai.alloc.management.query.service.GetAssignmentCandidates;
import com.moirai.alloc.management.query.service.GetAssignmentMembers;
import com.moirai.alloc.management.query.service.GetAssignmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private GetAssignedMembers getAssignedMembers;

    // 컨트롤러 생성자에 필요한 다른 의존성들도 MockBean으로 선언
    @MockBean private GetAssignmentCandidates getAssignmentCandidates;
    @MockBean private GetAssignmentMembers getAssignmentMembers;
    @MockBean private GetAssignmentStatus getAssignmentStatus;
    @MockBean private SelectAssignmentCandidates selectAssignmentCandidates;
    @MockBean private SelectAdditionalAssignmentCandidates selectAdditionalAssignmentCandidates;

    // PM 권한을 가진 사용자가 접근했을 때
    //  선정된 인력 목록을 정상적으로 반환하는지 검증
    @Test
    void getAssignedMembersPmSuccess() throws Exception {
        // given
        Long projectId = 1L;

        AssignedMemberDTO dto = new AssignedMemberDTO(
                10L,
                "김철수",
                "Backend",
                projectId
        );

        given(getAssignedMembers.getAssignedMembers(projectId))
                .willReturn(List.of(dto));

        // when & then
        mockMvc.perform(
                        get("/api/projects/{projectId}/assignments/assigned", projectId)
                                .with(authenticatedUser("PM"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(10L))
                .andExpect(jsonPath("$[0].employeeName").value("김철수"))
                .andExpect(jsonPath("$[0].jobName").value("Backend"));
    }
//     일반 USER 권한도 해당 API에 접근 가능함을 검증
//     반환 데이터가 없어도 200 OK를 내려주는지 확인
    @Test
    void getAssignedMembersUserSuccess() throws Exception {
        // given
        Long projectId = 1L;

        given(getAssignedMembers.getAssignedMembers(projectId))
                .willReturn(List.of());

        // when & then
        mockMvc.perform(
                        get("/api/projects/{projectId}/assignments/assigned", projectId)
                                .with(authenticatedUser("USER"))
                )
                .andExpect(status().isOk());
    }
    //인증 정보 없이 접근할 경우
    @Test
    void getAssignedMembersUnauthorized() throws Exception {
        mockMvc.perform(
                        get("/api/projects/{projectId}/assignments/assigned", 1L)
                )
                .andExpect(status().isForbidden());
    }
//    Controller가 요청을 처리하면서
//    GetAssignedMembers 유스케이스를 정확히 한 번 호출하는지 검증
    @Test
    void getAssignedMembersCallsServiceOnce() throws Exception {
        Long projectId = 1L;

        given(getAssignedMembers.getAssignedMembers(projectId))
                .willReturn(List.of());

        mockMvc.perform(
                        get("/api/projects/{projectId}/assignments/assigned", projectId)
                                .with(authenticatedUser("PM"))
                )
                .andExpect(status().isOk());

        verify(getAssignedMembers, times(1))
                .getAssignedMembers(projectId);
    }
}
