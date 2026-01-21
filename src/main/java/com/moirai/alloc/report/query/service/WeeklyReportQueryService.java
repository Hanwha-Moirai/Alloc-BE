package com.moirai.alloc.report.query.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportMissingResponse;
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

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

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
    public Page<WeeklyReportSummaryResponse> searchDocsReports(Long projectId,
                                                               WeeklyReportSearchCondition condition,
                                                               Pageable pageable) {
        WeeklyReportSearchCondition scopedCondition = new WeeklyReportSearchCondition(
                projectId,
                condition.projectName(),
                condition.reportStatus(),
                condition.weekStartFrom(),
                condition.weekStartTo()
        );
        return weeklyReportQueryRepository.search(scopedCondition, pageable);
    }

    @Transactional(readOnly = true)
    public WeeklyReportDetailResponse getDocsReportDetail(Long projectId, Long reportId) {
        WeeklyReportDetailResponse detail = weeklyReportQueryRepository.findDetail(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다."));
        if (!detail.projectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "주간 보고를 찾을 수 없습니다.");
        }
        return detail;
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

    @Transactional(readOnly = true)
    public List<WeeklyReportMissingResponse> getMissingWeeks(UserPrincipal principal,
                                                             Long projectId,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {
        if (!membershipRepository.existsMembership(projectId, principal.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "프로젝트 멤버가 아닙니다.");
        }
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 기간이 필요합니다.");
        }
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다.");
        }

        LocalDate rangeStart = toWeekStart(startDate);
        LocalDate rangeEnd = toWeekStart(endDate);
        List<LocalDate> reportedStarts = weeklyReportQueryRepository.findWeekStartDates(
                projectId,
                principal.userId(),
                rangeStart,
                rangeEnd
        );
        Set<LocalDate> reportedSet = new HashSet<>(reportedStarts);
        List<WeeklyReportMissingResponse> missing = new ArrayList<>();
        for (LocalDate cursor = rangeStart; !cursor.isAfter(rangeEnd); cursor = cursor.plusWeeks(1)) {
            if (reportedSet.contains(cursor)) {
                continue;
            }
            missing.add(new WeeklyReportMissingResponse(
                    cursor,
                    cursor.plusDays(6),
                    toWeekLabel(cursor)
            ));
        }
        return missing;
    }

    private LocalDate toWeekStart(LocalDate date) {
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.KOREA).getFirstDayOfWeek();
        return date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
    }

    private String toWeekLabel(LocalDate weekStartDate) {
        int week = weekStartDate.get(WeekFields.of(Locale.KOREA).weekOfMonth());
        return String.format("%d년 %d월 %d주차", weekStartDate.getYear(), weekStartDate.getMonthValue(), week);
    }
}
