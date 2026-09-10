package com.example.hr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.hr.domain.Dept;
import com.example.hr.dto.DeptListRow;

@Mapper
public interface DeptMapper {

    /** 부서 목록 + 소속 인원수 + 평균 급여 (EMP 조인·집계) */
    List<DeptListRow> selectAllWithStats();

    /** 검색 필터·폼 드롭다운용 — 부서 단순 목록 */
    List<Dept> selectAll();

    Dept selectById(@Param("deptId") Long deptId);

    void insert(Dept dept);

    void update(Dept dept);

    void deleteById(@Param("deptId") Long deptId);
}
