package com.moirai.alloc.management.query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class GetProjectDetail {
    public void getProjectDetail(Long projectId) {
    }
}
