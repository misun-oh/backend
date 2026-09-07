package com.example.hr.mapper;

import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpSearchCond;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Day 11 — 동적 검색·페이징을 위해 갱신된 EmpMapper.
 * 본문 1절(findList/countList), 5절(findPage/countPage), 3절(findByIds)를 병합.
 * XML: findPage/countPage 는 &lt;sql id="searchWhere"&gt; 를 공유(본문 5절).
 */
@Mapper
public interface EmpMapper {

    List<Emp> findList(EmpSearchCond cond);

    long countList(EmpSearchCond cond);

    // 페이지 데이터 : ORDER BY e.${sort.column} ${sort.direction} LIMIT #{size} OFFSET #{offset}
    List<Emp> findPage(EmpSearchCond cond);

    // 페이지네이션용 전체 건수 : findPage와 동일한 WHERE(<sql id="searchWhere">), LIMIT 없음
    long countPage(EmpSearchCond cond);

    List<Emp> findByIds(@Param("ids") List<Long> ids);
}
