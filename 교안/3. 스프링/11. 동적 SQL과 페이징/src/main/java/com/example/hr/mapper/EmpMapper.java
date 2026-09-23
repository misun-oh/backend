package com.example.hr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.hr.dto.EmpDto;
import com.example.hr.dto.EmpSearchCond;

/**
 * Day 11 — 동적 검색·페이징을 위해 갱신된 EmpMapper.
 * selectByCond : 1_동적SQL과검색.md 에서는 LIMIT 없이 전체 결과, 2_페이징.md 에서 LIMIT/OFFSET 추가.
 * countByCond  : 2_페이징.md 에서 추가 (selectByCond와 같은 &lt;sql id="searchWhere"&gt; 공유).
 * findByIds    : 1_동적SQL과검색.md 3절, EmpDto.empId 가 int 이므로 List&lt;Integer&gt; 사용.
 */
@Mapper
public interface EmpMapper {

    List<EmpDto> selectByCond(EmpSearchCond cond);

    int countByCond(EmpSearchCond cond);

    List<EmpDto> findByIds(@Param("ids") List<Integer> ids);
}
