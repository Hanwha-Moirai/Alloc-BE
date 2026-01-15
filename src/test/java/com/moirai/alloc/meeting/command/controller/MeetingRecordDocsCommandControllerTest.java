package com.moirai.alloc.meeting.command.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Sql(scripts = "/sql/meeting/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/meeting/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class MeetingRecordDocsCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("회의록 생성 요청이 성공한다.")
    void createMeetingRecord_returnsCreatedId() throws Exception {
        String body = """
                {
                  "projectId": 88001,
                  "progress": 30.0,
                  "meetingDate": "2025-01-10T10:00:00",
                  "meetingTime": "2025-01-10T10:00:00",
                  "agendas": [
                    {
                      "discussionTitle": "title",
                      "discussionContent": "content",
                      "discussionResult": "result",
                      "agendaType": "TYPE1"
                    }
                  ],
                  "participants": [
                    {
                      "userId": 88002,
                      "isHost": false
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/docs/meeting_record/create", 88001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("회의록 수정 요청이 성공한다.")
    void updateMeetingRecord_returnsOk() throws Exception {
        String body = """
                {
                  "meetingId": 88001,
                  "progress": 50.0,
                  "meetingDate": "2025-01-12T10:00:00",
                  "meetingTime": "2025-01-12T10:00:00",
                  "agendas": [
                    {
                      "discussionTitle": "updated",
                      "discussionContent": "content",
                      "discussionResult": "result",
                      "agendaType": "TYPE2"
                    }
                  ],
                  "participants": [
                    {
                      "userId": 88001,
                      "isHost": true
                    }
                  ]
                }
                """;

        mockMvc.perform(patch("/api/projects/{projectId}/docs/meeting_record/save", 88001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("회의록 삭제 요청이 성공한다.")
    void deleteMeetingRecord_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/docs/meeting_record/delete", 88001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .param("meetingId", "88001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private Authentication pmAuth() {
        UserPrincipal principal = new UserPrincipal(
                88001L,
                "pm_88001",
                "pm88001@example.com",
                "PM User",
                "PM",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
