package com.moirai.alloc.admin.query.mapper;

import com.moirai.alloc.admin.query.dto.AdminJobListItem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface AdminJobQueryMapper {

    List<AdminJobListItem> selectJobs(
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("q") String q
    );

    long countJobs(@Param("q") String q);
}