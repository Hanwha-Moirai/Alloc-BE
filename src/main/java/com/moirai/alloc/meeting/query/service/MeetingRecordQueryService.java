package com.moirai.alloc.meeting.query.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.meeting.query.dto.MeetingRecordDetailResponse;
import com.moirai.alloc.meeting.query.dto.MeetingRecordSearchCondition;
import com.moirai.alloc.meeting.query.dto.MeetingRecordSummaryResponse;
import com.moirai.alloc.meeting.query.repository.MeetingMembershipRepository;
import com.moirai.alloc.meeting.query.repository.MeetingRecordQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MeetingRecordQueryService {

    private final MeetingRecordQueryRepository meetingRecordQueryRepository;
    private final MeetingMembershipRepository membershipRepository;

    public MeetingRecordQueryService(MeetingRecordQueryRepository meetingRecordQueryRepository,
                                     MeetingMembershipRepository membershipRepository) {
        this.meetingRecordQueryRepository = meetingRecordQueryRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional(readOnly = true)
    public Page<MeetingRecordSummaryResponse> getDocsMeetingRecords(Pageable pageable) {
        return meetingRecordQueryRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<MeetingRecordSummaryResponse> searchDocsMeetingRecords(MeetingRecordSearchCondition condition,
                                                                       Pageable pageable) {
        return meetingRecordQueryRepository.search(condition, pageable);
    }

    @Transactional(readOnly = true)
    public MeetingRecordDetailResponse getDocsMeetingRecordDetail(Long meetingId) {
        return meetingRecordQueryRepository.findDetail(meetingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회의록을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<MeetingRecordSummaryResponse> getMyDocsMeetingRecords(UserPrincipal principal, Pageable pageable) {
        List<Long> projectIds = membershipRepository.findProjectIdsByUserId(principal.userId());
        return meetingRecordQueryRepository.findAllByProjectIds(projectIds, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MeetingRecordSummaryResponse> searchMyDocsMeetingRecords(UserPrincipal principal,
                                                                         MeetingRecordSearchCondition condition,
                                                                         Pageable pageable) {
        List<Long> projectIds = membershipRepository.findProjectIdsByUserId(principal.userId());
        return meetingRecordQueryRepository.searchInProjects(projectIds, condition, pageable);
    }

    @Transactional(readOnly = true)
    public MeetingRecordDetailResponse getMyDocsMeetingRecordDetail(UserPrincipal principal, Long meetingId) {
        MeetingRecordDetailResponse detail = meetingRecordQueryRepository.findDetail(meetingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회의록을 찾을 수 없습니다."));
        if (!membershipRepository.existsMembership(detail.projectId(), principal.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "프로젝트 멤버가 아닙니다.");
        }
        return detail;
    }
}
