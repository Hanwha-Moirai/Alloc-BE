package com.moirai.alloc.report.query.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportSearchCondition;
import com.moirai.alloc.report.query.dto.WeeklyReportSummaryResponse;
import com.moirai.alloc.report.query.repository.ReportMembershipRepository;
import com.moirai.alloc.report.query.repository.WeeklyReportQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WeeklyReportQueryService {

    private final WeeklyReportQueryRepository weeklyReportQueryRepository;
    private final ReportMembershipRepository membershipRepository;

    public WeeklyReportQueryService(WeeklyReportQueryRepository weeklyReportQueryRepository,
                                    ReportMembershipRepository membershipRepository) {
        this.weeklyReportQueryRepository = weeklyReportQueryRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional(readOnly = true)
    public Page<WeeklyReportSummaryResponse> getDocsReports(Long projectId, Pageable pageable) {
        return weeklyReportQueryRepository.findAllByProjectId(projectId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<WeeklyReportSummaryResponse> searchDocsReports(WeeklyReportSearchCondition condition,
                                                               Pageable pageable) {
        return weeklyReportQueryRepository.search(condition, pageable);
    }

    @Transactional(readOnly = true)
    public WeeklyReportDetailResponse getDocsReportDetail(Long reportId) {
        return weeklyReportQueryRepository.findDetail(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<WeeklyReportSummaryResponse> getMyDocsReports(UserPrincipal principal, Pageable pageable) {
        List<Long> projectIds = membershipRepository.findProjectIdsByUserId(principal.userId());
        return weeklyReportQueryRepository.findAllByProjectIds(projectIds, pageable);
    }

    @Transactional(readOnly = true)
    public Page<WeeklyReportSummaryResponse> searchMyDocsReports(UserPrincipal principal,
                                                                 WeeklyReportSearchCondition condition,
                                                                 Pageable pageable) {
        List<Long> projectIds = membershipRepository.findProjectIdsByUserId(principal.userId());
        return weeklyReportQueryRepository.searchInProjects(projectIds, condition, pageable);
    }

    @Transactional(readOnly = true)
    public WeeklyReportDetailResponse getMyDocsReportDetail(UserPrincipal principal, Long reportId) {
        WeeklyReportDetailResponse detail = weeklyReportQueryRepository.findDetail(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다."));
        if (!membershipRepository.existsMembership(detail.projectId(), principal.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "프로젝트 멤버가 아닙니다.");
        }
        return detail;
    }
}
