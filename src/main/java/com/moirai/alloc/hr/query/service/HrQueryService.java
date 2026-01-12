package com.moirai.alloc.hr.query.service;

import com.moirai.alloc.hr.command.domain.TechStandard;
import com.moirai.alloc.hr.command.repository.JobStandardRepository;
import com.moirai.alloc.hr.query.dto.JobStandardResponse;
import com.moirai.alloc.hr.query.dto.TechStackCursor;
import com.moirai.alloc.hr.query.dto.TechStackDropdownResponse;
import com.moirai.alloc.hr.query.dto.TechStandardResponse;
import com.moirai.alloc.hr.command.repository.TechStandardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HrQueryService {
    private final TechStandardRepository techStandardRepository;
    private final JobStandardRepository jobStandardRepository;

    /* 직군 드롭다운 */
    public List<JobStandardResponse> getJobs() {
        return jobStandardRepository.findAllByOrderByJobNameAsc()
                .stream()
                .map(j -> new JobStandardResponse(j.getJobId(), j.getJobName()))
                .toList();
    }

    /* 드롭다운 스크롤 + 검색 keyword가 바뀌면 프론트는 cursor를 비워서(초기화) 다시 요청해야 함 */
    public TechStackDropdownResponse getTechStacksDropdown(String keyword, String cursorTechName, Long cursorTechId, Integer size) {
        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), 50);

        // size+1로 받아서 hasNext 판정
        PageRequest pr = PageRequest.of(0, safeSize + 1);

        String kw = (keyword == null) ? "" : keyword.trim();
        boolean hasKeyword = !kw.isEmpty();

        List<TechStandard> fetched = hasKeyword
                ? techStandardRepository.searchNextTechStacks(kw, cursorTechName, cursorTechId, pr)
                : techStandardRepository.findNextTechStacks(cursorTechName, cursorTechId, pr);

        boolean hasNext = fetched.size() > safeSize;
        List<TechStandard> current = hasNext ? fetched.subList(0, safeSize) : fetched;

        List<TechStandardResponse> items = current.stream()
                .map(t -> new TechStandardResponse(t.getTechId(), t.getTechName()))
                .toList();

        TechStackCursor nextCursor;
        if (items.isEmpty()) {
            nextCursor = TechStackCursor.empty();
        } else {
            TechStandard last = current.get(current.size() - 1);
            nextCursor = new TechStackCursor(last.getTechName(), last.getTechId());
        }

        return new TechStackDropdownResponse(items, hasNext, nextCursor);
    }
}
