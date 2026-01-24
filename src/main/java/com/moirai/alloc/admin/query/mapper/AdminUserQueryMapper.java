package com.moirai.alloc.admin.query.mapper;

import com.moirai.alloc.admin.query.dto.AdminUserListItem;
import com.moirai.alloc.admin.query.dto.AdminUserMetaResponse;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface AdminUserQueryMapper {
    //사용자 조회
    List<AdminUserListItem> selectUsers(@Param("limit") int limit,
                                        @Param("offset") int offset,
                                        @Param("q") String q,
                                        @Param("role") String role,
                                        @Param("status") String status);

    long countUsers(@Param("q") String q,
                    @Param("role") String role,
                    @Param("status") String status);

    //사용자 등록/수정 시 드롭다운 조회
    List<AdminUserMetaResponse.IdLabel> selectTitleOptions();

    List<AdminUserMetaResponse.IdLabel> selectDepartmentOptions();


}
