package com.moirai.alloc.meeting.query.service;

import com.moirai.alloc.meeting.command.domain.command.domain.entity.MeetingRecordLog;
import com.moirai.alloc.meeting.command.repository.MeetingRecordLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingRecordLogQueryService {

    private static final int DEFAULT_SIZE = 10;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final MeetingRecordLogRepository meetingRecordLogRepository;

    public List<String> getLogs(Long projectId, Integer size) {
        int limit = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        return meetingRecordLogRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId, PageRequest.of(0, limit))
                .stream()
                .map(this::formatLog)
                .toList();
    }

    private String formatLog(MeetingRecordLog log) {
        String time = log.getCreatedAt() == null ? "-" : log.getCreatedAt().format(FORMATTER);
        return "[" + time + "] " + log.getLogMessage();
    }
}
