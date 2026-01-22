package com.moirai.alloc.admin.query.mapper;

import com.moirai.alloc.admin.query.dto.AdminTechStackListItem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface AdminTechStackQueryMapper {

    //기술 스택 조회
    List<AdminTechStackListItem> selectTechStacks(
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("q") String q
    );

    long countTechStacks(@Param("q") String q);
}
