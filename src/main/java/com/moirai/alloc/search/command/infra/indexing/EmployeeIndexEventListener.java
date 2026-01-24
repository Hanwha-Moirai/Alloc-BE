package com.moirai.alloc.search.command.infra.indexing;

import com.moirai.alloc.search.command.service.IndexPeopleToSearch;
import com.moirai.alloc.search.command.event.EmployeeAssignmentChangedEvent;
import com.moirai.alloc.search.command.event.EmployeeProfileChangedEvent;
import com.moirai.alloc.search.command.event.EmployeeSkillChangedEvent;
import org.springframework.context.event.EventListener;

public class EmployeeIndexEventListener {
    // 도메인 이벤트를 받아서 검색 인덱싱 유스케이스를 호출

    private final IndexPeopleToSearch indexPeopleToSearch;

    public EmployeeIndexEventListener(IndexPeopleToSearch indexPeopleToSearch) {
        this.indexPeopleToSearch = indexPeopleToSearch;
    }

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
