package com.moirai.alloc.notification.command.repository;

import com.moirai.alloc.notification.command.domain.entity.AlarmTemplate;
import com.moirai.alloc.notification.command.domain.entity.AlarmTemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlarmTemplateRepository extends JpaRepository<AlarmTemplate, Long> {

    Optional<AlarmTemplate> findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(AlarmTemplateType type);
}
