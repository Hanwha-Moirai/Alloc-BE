package com.moirai.alloc.meeting.query.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.meeting.query.dto.MeetingRecordSearchCondition;
import com.moirai.alloc.meeting.query.dto.response.MeetingRecordDetailResponse;
import com.moirai.alloc.meeting.query.dto.response.MeetingRecordSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Sql(scripts = "/sql/meeting/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
//@Sql(scripts = "/sql/meeting/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class MeetingRecordQueryServiceTest {

    private static final Long MEETING_ID = 88001L;
    private static final Long PROJECT_ID = 88001L;
    private static final Long PM_USER_ID = 88001L;

    @Autowired
    private MeetingRecordQueryService meetingRecordQueryService;

    @Test
    void getDocsMeetingRecords_returnsPage() {
        Page<MeetingRecordSummaryResponse> page =
                meetingRecordQueryService.getDocsMeetingRecords(PROJECT_ID, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
    }

    @Test
    void searchDocsMeetingRecords_filtersByProjectName() {
        MeetingRecordSearchCondition condition = new MeetingRecordSearchCondition(
                "Meeting Project",
                null,
                null
        );

        Page<MeetingRecordSummaryResponse> page =
                meetingRecordQueryService.searchDocsMeetingRecords(PROJECT_ID, condition, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
    }

    @Test
    void getMyDocsMeetingRecordDetail_checksMembership() {
        UserPrincipal principal = new UserPrincipal(
                PM_USER_ID,
                "pm_88001",
                "pm88001@example.com",
                "PM",
                "PM",
                "pw"
        );

        MeetingRecordDetailResponse detail =
                meetingRecordQueryService.getMyDocsMeetingRecordDetail(principal, MEETING_ID);

        assertThat(detail.meetingId()).isEqualTo(MEETING_ID);
    }
}
