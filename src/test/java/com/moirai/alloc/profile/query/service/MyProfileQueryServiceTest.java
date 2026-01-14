package com.moirai.alloc.profile.query.service;

import com.moirai.alloc.profile.query.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
//@Sql(scripts = "/sql/profile/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
//@Sql(scripts = "/sql/profile/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class MyProfileQueryServiceTest {

    private static final Long USER_ID = 77001L;
    private static final Long USER_ID_NO_STACKS = 77002L;
    private static final Long USER_ID_NO_HISTORY = 77003L;

    @Autowired
    private MyProfileQueryService myProfileQueryService;

    @Test
    @DisplayName("상단바 프로필 요약 정보 조회를 성공한다")
    void getMySummary_success() {
        MyProfileSummaryResponse response = myProfileQueryService.getMySummary(USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getUserName()).isEqualTo("김명진");
        assertThat(response.getJobName()).isEqualTo("BackendDeveloper");
        assertThat(response.getTitleName()).isEqualTo("IT");
        assertThat(response.getProfileImageUrl()).isEqualTo("profile.jpg");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 상단바 프로필 요약 조회 시 예외를 발생시킨다")
    void getMySummary_throwsException_whenUserNotFound() {
        Long notExistsUserId = 999999L;

        assertThatThrownBy(() -> myProfileQueryService.getMySummary(notExistsUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자 ID로 내 프로필 기본 정보 조회를 성공한다")
    void getMyProfile_success() {
        MyProfileBasicResponse response = myProfileQueryService.getMyProfile(USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getUserName()).isEqualTo("김명진");
        assertThat(response.getDeptName()).isEqualTo("IT");
        assertThat(response.getJobName()).isEqualTo("BackendDeveloper");
        assertThat(response.getEmail()).isEqualTo("kmj@alloc.co.kr");
        assertThat(response.getPhone()).isEqualTo("010-1234-5678");
        assertThat(response.getHiringDate()).isEqualTo(LocalDate.of(2022, 3, 1));
        assertThat(response.isAssignedNow()).isTrue(); // ACTIVE 프로젝트 + ASSIGNED 조건 충족
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 프로필 조회 시 예외를 발생시킨다")
    void getMyProfile_throwsException_whenUserNotFound() {
        Long notExistsUserId = 999999L;

        assertThatThrownBy(() -> myProfileQueryService.getMyProfile(notExistsUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("내 기술 스택 목록 조회를 성공한다")
    void getMyTechStacks_success() {
        List<MyTechStackResponse> response = myProfileQueryService.getMyTechStacks(USER_ID);

        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();

        // 너 테스트 의도대로 Java, Python 포함되게 setup.sql에 넣어둠
        assertThat(response).extracting(MyTechStackResponse::getTechName)
                .contains("Java", "Python");

        // 샘플 숙련도 값도 확인 (정확히 어떤 값이든 상관없으면 contains만)
        assertThat(response).extracting(MyTechStackResponse::getProficiency)
                .contains("LV3", "LV2");
    }

    @Test
    @DisplayName("내 기술 스택이 없을 경우 빈 리스트를 반환한다")
    void getMyTechStacks_returnsEmptyList_whenNoStacks() {
        List<MyTechStackResponse> response = myProfileQueryService.getMyTechStacks(USER_ID_NO_STACKS);

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("프로젝트 히스토리 조회를 성공한다")
    void getMyProjectHistory_success() {
        int page = 0;
        int size = 5;

        List<MyProjectHistoryResponse> response =
                myProfileQueryService.getMyProjectHistory(USER_ID, page, size);

        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();

        MyProjectHistoryResponse projectA = response.stream()
                .filter(p -> p.getProjectId().equals(101L))
                .findFirst()
                .orElseThrow();

        assertThat(projectA.getProjectName()).isEqualTo("Project A");
        assertThat(projectA.getContributedTechs()).hasSize(2);
        assertThat(projectA.getContributedTechs())
                .extracting(MyProjectHistoryResponse.ContributedTech::getTechName)
                .containsExactlyInAnyOrder("Java", "Spring");

        MyProjectHistoryResponse projectB = response.stream()
                .filter(p -> p.getProjectId().equals(102L))
                .findFirst()
                .orElseThrow();

        assertThat(projectB.getProjectName()).isEqualTo("Project B");
        assertThat(projectB.getContributedTechs()).hasSize(1);
        assertThat(projectB.getContributedTechs().get(0).getTechName()).isEqualTo("JPA");
    }

    @Test
    @DisplayName("프로젝트 히스토리가 없을 경우 빈 리스트를 반환한다")
    void getMyProjectHistory_returnsEmptyList_whenNoHistory() {
        int page = 0;
        int size = 5;

        List<MyProjectHistoryResponse> response =
                myProfileQueryService.getMyProjectHistory(USER_ID_NO_HISTORY, page, size);

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }
}
