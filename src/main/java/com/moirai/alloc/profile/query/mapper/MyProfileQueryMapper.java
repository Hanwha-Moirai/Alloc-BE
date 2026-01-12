package com.moirai.alloc.profile.query.mapper;

import com.moirai.alloc.profile.query.dto.MyProfileBasicResponse;
import com.moirai.alloc.profile.query.dto.MyProjectHistoryRow;
import com.moirai.alloc.profile.query.dto.MyTechStackResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyProfileQueryMapper {

        // 1) 내 기본 정보
        MyProfileBasicResponse selectMyProfile(@Param("userId") Long userId);

        // 2) 내 기술 스택
        List<MyTechStackResponse> selectMyTechStacks(@Param("userId") Long userId);

        // 3-1) 프로젝트 히스토리(카드 단위) - 프로젝트 ID만 페이징으로 먼저 조회
        List<Long> selectMyProjectIdsForHistory(
                @Param("userId") Long userId,
                @Param("offset") int offset,
                @Param("size") int size
        );

        // 3-2) 위에서 뽑은 projectIds에 대해서만 기여 기술 row 조회
        List<MyProjectHistoryRow> selectMyProjectHistoryRowsByProjectIds(
                @Param("userId") Long userId,
                @Param("projectIds") List<Long> projectIds
        );
    }

