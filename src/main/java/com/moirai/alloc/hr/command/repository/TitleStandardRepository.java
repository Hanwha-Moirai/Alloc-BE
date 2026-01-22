package com.moirai.alloc.hr.command.repository;

import com.moirai.alloc.hr.command.domain.TitleStandard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TitleStandardRepository extends JpaRepository<TitleStandard, Long> {

    boolean existsByTitleNameIgnoreCase(String titleName);
}
