package com.moirai.alloc.user.query.mapper;

import com.moirai.alloc.user.query.dto.UserListItem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface UserQueryMapper {
    List<UserListItem> selectUsers(
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
