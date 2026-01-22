package com.moirai.alloc.admin.query.mapper;

import com.moirai.alloc.admin.query.dto.AdminTitleStandardListItem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface AdminTitleStandardQueryMapper {
    List<AdminTitleStandardListItem> selectTitleStandard(
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("q") String q
    );

    long countTitleStandard(@Param("q") String q);
}
