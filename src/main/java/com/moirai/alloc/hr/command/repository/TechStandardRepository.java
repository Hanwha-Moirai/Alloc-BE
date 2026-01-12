package com.moirai.alloc.hr.command.repository;

import com.moirai.alloc.hr.command.domain.TechStandard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TechStandardRepository extends JpaRepository<TechStandard, Long> {

    /* keyword 없이: 커서 이후 목록 techName ASC, techId ASC */
    @Query("""
        select t
        from TechStandard t
        where (:cursorName is null or :cursorId is null)
           or (t.techName > :cursorName)
           or (t.techName = :cursorName and t.techId > :cursorId)
        order by t.techName asc, t.techId asc
    """)
    List<TechStandard> findNextTechStacks(
            @Param("cursorName") String cursorName,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /* keyword 포함: 커서 이후 목록 like 검색 + 커서 조건 */
    @Query("""
        select t
        from TechStandard t
        where lower(t.techName) like lower(concat('%', :keyword, '%'))
          and (
                (:cursorName is null or :cursorId is null)
             or (t.techName > :cursorName)
             or (t.techName = :cursorName and t.techId > :cursorId)
          )
        order by t.techName asc, t.techId asc
    """)
    List<TechStandard> searchNextTechStacks(
            @Param("keyword") String keyword,
            @Param("cursorName") String cursorName,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
