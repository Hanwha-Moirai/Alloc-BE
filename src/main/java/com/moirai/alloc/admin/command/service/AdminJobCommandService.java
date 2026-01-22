package com.moirai.alloc.admin.command.service;

import com.moirai.alloc.common.exception.ErrorCode;
import com.moirai.alloc.common.exception.NotFoundException;
import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.repository.JobStandardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AdminJobCommandService {

    private final JobStandardRepository jobStandardRepository;

    public AdminJobCommandService(JobStandardRepository jobStandardRepository) {
        this.jobStandardRepository = jobStandardRepository;
    }

    public Long createJob(String jobName) {
        String displayName = normalizeJobName(jobName);
        String norm = normalizeKey(jobName);

        if (jobStandardRepository.existsByJobNameNorm(norm)) {
            throw new IllegalArgumentException("이미 존재하는 직무입니다.");
        }

        JobStandard saved = jobStandardRepository.save(
                JobStandard.builder()
                        .jobName(displayName)
                        .build()
        );
        return saved.getJobId();
    }

    public Long updateJob(Long jobId, String jobName) {
        String displayName = normalizeJobName(jobName);
        String norm = normalizeKey(jobName);

        JobStandard job = jobStandardRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("직무를 찾을 수 없습니다."));

        if (jobStandardRepository.existsByJobNameNormAndJobIdNot(norm, jobId)) {
            throw new IllegalArgumentException("이미 존재하는 직무입니다.");
        }

        job.updateJobName(displayName);
        return job.getJobId();
    }

    public Long deleteJob(Long jobId) {
        JobStandard jobStandard = jobStandardRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("직무를 찾을 수 없습니다."));
        jobStandardRepository.delete(jobStandard);
        return jobId;
    }

    private String normalizeJobName(String jobName) {
        if (jobName == null) throw new IllegalArgumentException("직무 이름이 필요합니다.");

        String normalized = jobName
                .replace('\u00A0', ' ')
                .trim()
                .replaceAll("\\s+", " "); // 연속 공백 정리

        if (normalized.isEmpty()) throw new IllegalArgumentException("직무 이름이 필요합니다.");
        return normalized;
    }

    private String normalizeKey(String jobName) {
        // 공백 제거 + 소문자
        return normalizeJobName(jobName).replace(" ", "").toLowerCase();
    }
}