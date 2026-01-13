package com.moirai.alloc.meeting.query.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Sql(scripts = "/sql/meeting/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class MeetingRecordDocsQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void getMeetingRecords_returnsPage() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/docs/meeting_record", 88001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].projectName").value("Meeting Project"));
    }

    @Test
    @WithMockUser
    void searchMeetingRecords_returnsMatches() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/docs/meeting_record/search", 88001)
                        .param("keyword", "검색키워드"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].meetingId").value(88001))
                .andExpect(jsonPath("$.data.content[0].projectName").value("Meeting Project"));
    }

    @Test
    @WithMockUser
    void getMeetingRecordDetail_returnsDetail() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/docs/meeting_record/{meetingRecordId}", 88001, 88001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meetingId").value(88001));
    }
}
