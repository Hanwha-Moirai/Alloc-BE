package com.moirai.alloc.search.infra.indexing;

import com.moirai.alloc.search.command.service.IndexPeopleToSearch;
import com.moirai.alloc.search.domain.event.EmployeeAssignmentChangedEvent;
import com.moirai.alloc.search.domain.event.EmployeeProfileChangedEvent;
import com.moirai.alloc.search.domain.event.EmployeeSkillChangedEvent;
import org.springframework.context.event.EventListener;

public class EmployeeIndexEventListener {
    // 연결하는 역할. 도메인 이벤트와 검색 커멘드서비스 연결

    private final IndexPeopleToSearch indexPeopleToSearch;

    @EventListener
    public void on(EmployeeProfileChangedEvent event){
        indexPeopleToSearch.indexEmployee(event.getEmployeeId());
    }

    @EventListener
    public void on(EmployeeSkillChangedEvent event){
        indexPeopleToSearch.indexEmployee(event.getEmployeeId());
    }

    @EventListener
    public void on(EmployeeAssignmentChangedEvent event){
        indexPeopleToSearch.indexEmployee(event.getEmployeeId());
    }
}
