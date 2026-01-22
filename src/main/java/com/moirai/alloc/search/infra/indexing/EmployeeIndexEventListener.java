package com.moirai.alloc.search.infra.indexing;

import com.moirai.alloc.search.command.service.IndexPeopleToSearch;
import com.moirai.alloc.search.domain.event.EmployeeAssignmentChangedEvent;
import com.moirai.alloc.search.domain.event.EmployeeProfileChangedEvent;
import com.moirai.alloc.search.domain.event.EmployeeSkillChangedEvent;
import org.springframework.context.event.EventListener;

public class EmployeeIndexEventListener {
    // 도메인 이벤트를 받아서 검색 인덱싱 유스케이스를 호출

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
