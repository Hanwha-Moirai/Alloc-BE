package com.moirai.alloc.admin.query.mapper;

import com.moirai.alloc.admin.query.dto.AdminUserListItem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface AdminUserQueryMapper {
    List<AdminUserListItem> selectUsers(
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("q") String q,
            @Param("role") String role
    );

    long countUsers(
            @Param("q") String q,
            @Param("role") String role
    );

}
