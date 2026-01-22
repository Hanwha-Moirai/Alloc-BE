package com.moirai.alloc.admin.command.service;

import com.moirai.alloc.common.exception.NotFoundException;
import com.moirai.alloc.hr.command.domain.TitleStandard;
import com.moirai.alloc.hr.command.repository.TitleStandardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminTitleStandardCommandService {

    private final TitleStandardRepository titleStandardRepository;

    public AdminTitleStandardCommandService(TitleStandardRepository titleStandardRepository) {
        this.titleStandardRepository = titleStandardRepository;
    }

    public Long createTitle(String titleName, Integer monthlyCost) {
        String normalized = normalizeTitleName(titleName);

        if (titleStandardRepository.existsByTitleNameIgnoreCase(normalized)) {
            throw new IllegalArgumentException("이미 존재하는 직급입니다.");
        }

        TitleStandard saved = titleStandardRepository.save(
                TitleStandard.builder()
                        .titleName(normalized)
                        .monthlyCost(monthlyCost)
                        .build()
        );

        return saved.getTitleStandardId();
    }

    public Long updateTitle(Long titleId, String titleName, Integer monthlyCost) {
        String normalized = normalizeTitleName(titleName);

        TitleStandard title = titleStandardRepository.findById(titleId)
                .orElseThrow(() -> new NotFoundException("직급을 찾을 수 없습니다."));

        boolean duplicated =
                titleStandardRepository.existsByTitleNameIgnoreCase(normalized)
                        && !title.getTitleName().equalsIgnoreCase(normalized);

        if (duplicated) {
            throw new IllegalArgumentException("이미 존재하는 직급입니다.");
        }

        title.titleUpdate(normalized, monthlyCost);

        return title.getTitleStandardId();
    }

    private String normalizeTitleName(String titleName) {
        if (titleName == null) {
            throw new IllegalArgumentException("직급 이름이 필요합니다.");
        }
        String normalized = titleName.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("직급 이름이 필요합니다.");
        }
        return normalized;
    }
}
