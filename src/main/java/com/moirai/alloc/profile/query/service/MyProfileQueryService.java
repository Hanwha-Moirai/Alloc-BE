package com.moirai.alloc.profile.query.service;

import com.moirai.alloc.profile.query.dto.MyProfileBasicResponse;
import com.moirai.alloc.profile.query.dto.MyProjectHistoryResponse;
import com.moirai.alloc.profile.query.dto.MyProjectHistoryRow;
import com.moirai.alloc.profile.query.dto.MyTechStackResponse;
import com.moirai.alloc.profile.query.mapper.MyProfileQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyProfileQueryService {

    private final MyProfileQueryMapper myProfileQueryMapper;

    public MyProfileBasicResponse getMyProfile(Long userId) {
        MyProfileBasicResponse res = myProfileQueryMapper.selectMyProfile(userId);
        if (res == null) {
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
        }
        return res;
    }

    public List<MyTechStackResponse> getMyTechStacks(Long userId) {
        return myProfileQueryMapper.selectMyTechStacks(userId);
    }

    /* 프로젝트 히스토리 무한스크롤
     * 1단계: 프로젝트ID를 페이징으로 size만큼 가져온다 (카드 단위)
     * 2단계: 해당 projectIds에 대해서만 기여 기술 row를 조인해서 가져온다
     * 3단계: projectId 기준으로 그룹핑하여 카드 구조로 변환한다
     */
    public List<MyProjectHistoryResponse> getMyProjectHistory(Long userId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 30);
        int offset = safePage * safeSize;

        // (1) 카드 단위로 projectIds를 가져옴
        List<Long> projectIds = myProfileQueryMapper.selectMyProjectIdsForHistory(userId, offset, safeSize);
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }

        // (2) 잘라낸 projectIds 범위 내에서만 row 조회
        List<MyProjectHistoryRow> rows =
                myProfileQueryMapper.selectMyProjectHistoryRowsByProjectIds(userId, projectIds);

        // (3) row -> 카드 그룹핑
        return groupProjectHistory(rows);
    }

    private List<MyProjectHistoryResponse> groupProjectHistory(List<MyProjectHistoryRow> rows) {
        Map<Long, TempProject> map = new LinkedHashMap<>();

        for (MyProjectHistoryRow r : rows) {
            TempProject tp = map.computeIfAbsent(
                    r.getProjectId(),
                    k -> new TempProject(
                            r.getProjectId(),
                            r.getProjectName(),
                            r.getStartDate(),
                            r.getEndDate(),
                            r.getMyJobName(),
                            new ArrayList<>()
                    )
            );

            if (r.getTechId() != null) {
                tp.contributed.add(new MyProjectHistoryResponse.ContributedTech(
                        r.getTechId(),
                        r.getTechName(),
                        r.getProficiency()
                ));
            }
        }

        return map.values().stream()
                .map(tp -> new MyProjectHistoryResponse(
                        tp.projectId,
                        tp.projectName,
                        tp.startDate,
                        tp.endDate,
                        tp.myJobName,
                        tp.contributed
                ))
                .toList();
    }

    private static class TempProject {
        Long projectId;
        String projectName;
        LocalDate startDate;
        LocalDate endDate;
        String myJobName;
        List<MyProjectHistoryResponse.ContributedTech> contributed;

        TempProject(Long projectId, String projectName, LocalDate startDate, LocalDate endDate,
                    String myJobName, List<MyProjectHistoryResponse.ContributedTech> contributed) {
            this.projectId = projectId;
            this.projectName = projectName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.myJobName = myJobName;
            this.contributed = contributed;
        }
    }
}
