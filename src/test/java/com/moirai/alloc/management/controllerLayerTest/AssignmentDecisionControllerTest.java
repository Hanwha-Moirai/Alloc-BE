package com.moirai.alloc.management.controllerLayerTest;

import com.moirai.alloc.management.api.AssignmentDecisionController;
import com.moirai.alloc.management.command.service.AcceptAssignment;
import com.moirai.alloc.management.command.service.DecideFinalAssignment;
import com.moirai.alloc.management.command.service.RequestInterview;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssignmentDecisionController.class)
class AssignmentDecisionControllerTest extends ControllerTestSupport {

    @Autowired
    MockMvc mockMvc;

    @MockBean private AcceptAssignment acceptAssignment;
    @MockBean private RequestInterview requestInterview;
    @MockBean private DecideFinalAssignment decideFinalAssignment;
    /**
     * [POST /assignments/{assignmentId}/response]
     * - 일반 사용자(USER)가 배치 요청에 응답할 수 있는지 검증
     * - RequestParam(status) 매핑 검증
     */
    @Test
    void respondAssignment_returnsOk_forUser() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments/10/response")
                        .with(authenticatedUser("USER"))
                        .param("status", "ACCEPTED"))
                .andExpect(status().isOk());
    }
    /**
     * [POST /assignments/{assignmentId}/response]
     * - PM 사용자는 배치 응답 권한이 없음을 검증
     * - Role 기반 접근 제어 → 403 Forbidden
     */
    @Test
    void respondAssignment_returnsForbidden_forPm() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments/10/response")
                        .with(authenticatedUser("PM"))
                        .param("status", "ACCEPTED"))
                .andExpect(status().isForbidden());
    }
    /**
     * [POST /assignments/{assignmentId}/decision]
     * - PM 권한 사용자가 최종 배치 결정을 내릴 수 있는지 검증
     * - RequestParam(decision) 매핑 및 권한 검증
     */
    @Test
    void decideAssignment_returnsOk_forPm() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments/10/decision")
                        .with(authenticatedUser("PM"))
                        .param("decision", "ASSIGNED"))
                .andExpect(status().isOk());
    }
    /**
     * [POST /assignments/{assignmentId}/decision]
     * - 일반 사용자(USER)의 최종 배치 결정 시도 차단 검증
     */
    @Test
    void decideAssignment_returnsForbidden_forUser() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments/10/decision")
                        .with(authenticatedUser("USER"))
                        .param("decision", "ASSIGNED"))
                .andExpect(status().isForbidden());
    }
    /**
     * [POST /assignments/{assignmentId}/decision]
     * - CSRF 토큰이 있더라도
     *   Role 권한이 없으면 접근이 차단되는지 검증
     * - 보안 설정의 우선순위 검증
     */
    @Test
    void decideAssignment_returnsForbidden_forUserRole() throws Exception {
        mockMvc.perform(post("/api/projects/1/assignments/10/decision")
                        .with(authenticatedUser("USER"))
                        .param("decision", "ASSIGNED")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

}
