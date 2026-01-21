package com.moirai.alloc.notification.command.init;

import com.moirai.alloc.notification.command.domain.entity.*;
import com.moirai.alloc.notification.command.repository.AlarmTemplateRepository;
import com.moirai.alloc.notification.common.contract.AlarmTemplateType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlarmTemplateInitializer implements ApplicationRunner {

    private final AlarmTemplateRepository templateRepository;

    @Override
    public void run(ApplicationArguments args) {
        upsert(AlarmTemplateType.POST_TEMP,       "프로젝트 임시 배정", "프로젝트 {{projectName}} 임시멤버로 지정되었습니다.");
        upsert(AlarmTemplateType.POST_FINAL,       "프로젝트 최종 배정", "프로젝트 {{projectName}} 최종멤버로 지정되었습니다.");
        upsert(AlarmTemplateType.TASK_ASSIGN,       "태스크 담당자 배정", "태스크 {{taskName}} 담당자로 지정되었습니다.");
        upsert(AlarmTemplateType.TASK_DUE_D1,       "마감 임박(D-1)",     "태스크 {{taskName}} 마감이 1일 남았습니다.");
        upsert(AlarmTemplateType.TASK_DUE_D3,       "마감 임박(D-3)",     "태스크 {{taskName}} 마감이 3일 남았습니다.");
        upsert(AlarmTemplateType.SCHEDULE_INVITE,   "일정 초대",          "일정 {{eventName}} 에 초대되었습니다.");
        upsert(AlarmTemplateType.SCHEDULE_CHANGE,   "일정 변경",          "일정 {{eventName}} 정보가 변경되었습니다.");
        upsert(AlarmTemplateType.MILESTONE,         "마일스톤 생성",      "마일스톤 {{milestoneName}} 이 생성되었습니다.");
        upsert(AlarmTemplateType.WEEKLY_REPORT,         "주간보고서 생성",      "주간보고서 {{weeklyReportName}} 이 생성되었습니다.");
    }

    private void upsert(AlarmTemplateType type, String title, String context) {
        templateRepository.findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(type)
                .ifPresentOrElse(
                        t -> { /* 이미 존재하면 유지(운영 정책에 따라 update 가능) */ },
                        () -> templateRepository.save(
                                AlarmTemplate.builder()
                                        .alarmTemplateType(type)
                                        .templateTitle(title)
                                        .templateContext(context)
                                        .build()
                        )
                );
    }
}
