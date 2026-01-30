package com.moirai.alloc.gantt.query.application;

import com.moirai.alloc.gantt.query.dto.request.DelayedTaskSearchRequest;
import com.moirai.alloc.gantt.query.dto.response.DelayedTaskResponse;
import com.moirai.alloc.gantt.query.repository.DelayedTaskQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DelayedTaskQueryService {

    private final DelayedTaskQueryRepository delayedTaskQueryRepository;

    public DelayedTaskQueryService(DelayedTaskQueryRepository delayedTaskQueryRepository) {
        this.delayedTaskQueryRepository = delayedTaskQueryRepository;
    }

    public List<DelayedTaskResponse> findDelayedTasks(DelayedTaskSearchRequest request) {
        return delayedTaskQueryRepository.findDelayedTasks(request);
    }
}
