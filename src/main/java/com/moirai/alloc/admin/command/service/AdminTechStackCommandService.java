package com.moirai.alloc.admin.command.service;

import com.moirai.alloc.hr.command.domain.TechStandard;
import com.moirai.alloc.hr.command.repository.TechStandardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AdminTechStackCommandService {

    private final TechStandardRepository techStandardRepository;

    public AdminTechStackCommandService(TechStandardRepository techStandardRepository) {
        this.techStandardRepository = techStandardRepository;
    }

    public Long createTechStack(String techName) {
        String normalized = normalizeTechName(techName);
        if (techStandardRepository.existsByTechNameIgnoreCase(normalized)) {
            throw new IllegalArgumentException("이미 존재하는 기술 스택입니다.");
        }
        TechStandard saved = techStandardRepository.save(
                TechStandard.builder()
                        .techName(normalized)
                        .build()
        );
        return saved.getTechId();
    }

    public Long updateTechStack(Long techId, String techName) {
        String normalized = normalizeTechName(techName);
        TechStandard techStandard = techStandardRepository.findById(techId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "기술 스택을 찾을 수 없습니다."));
        if (techStandardRepository.existsByTechNameIgnoreCase(normalized)
                && !techStandard.getTechName().equalsIgnoreCase(normalized)) {
            throw new IllegalArgumentException("이미 존재하는 기술 스택입니다.");
        }
        techStandard.updateTechName(normalized);
        return techStandard.getTechId();
    }

    public Long deleteTechStack(Long techId) {
        TechStandard techStandard = techStandardRepository.findById(techId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "기술 스택을 찾을 수 없습니다."));
        techStandardRepository.delete(techStandard);
        return techId;
    }

    private String normalizeTechName(String techName) {
        if (techName == null) {
            throw new IllegalArgumentException("기술 스택 이름이 필요합니다.");
        }
        String normalized = techName.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("기술 스택 이름이 필요합니다.");
        }
        return normalized;
    }
}
