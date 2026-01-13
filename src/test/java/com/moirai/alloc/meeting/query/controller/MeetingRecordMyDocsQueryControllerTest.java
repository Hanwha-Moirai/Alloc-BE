package com.moirai.alloc.meeting.query.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
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
class MeetingRecordMyDocsQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMyMeetingRecords_returnsPage() throws Exception {
        mockMvc.perform(get("/api/mydocs/meeting_record")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].projectName").value("Meeting Project"));
    }

    @Test
    void searchMyMeetingRecords_returnsMatches() throws Exception {
        mockMvc.perform(get("/api/mydocs/meeting_record/search")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .param("keyword", "검색키워드"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].meetingId").value(88001))
                .andExpect(jsonPath("$.data.content[0].projectName").value("Meeting Project"));
    }

    @Test
    void getMyMeetingRecordDetail_returnsDetail() throws Exception {
        mockMvc.perform(get("/api/mydocs/meeting_record/{meetingRecordId}", 88001)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meetingId").value(88001));
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
