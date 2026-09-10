package com.example.hr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpSearchCond;

@Mapper
public interface EmpMapper {

    List<Emp> selectByCond(EmpSearchCond cond);

    long countByCond(EmpSearchCond cond);

    Emp selectById(@Param("empId") Long empId);

    void insert(Emp emp);

    void update(Emp emp);

    void deleteById(@Param("empId") Long empId);

    int countByDeptId(@Param("deptId") Long deptId);

    /** 관리자·수정 폼 드롭다운용 — 재직 중인 사원 목록 */
    List<Emp> selectActiveForDropdown();

    /** 사원 상세 "조직" 탭 — 부하 직원 목록 */
    List<Emp> selectSubordinates(@Param("managerId") Long managerId);

    /** 대시보드 — 최근 입사자 */
    List<Emp> selectRecentHires(@Param("limit") int limit);

    long countAll();

    long countActive();

    long avgSalary();

    long maxSalary();

    long minSalary();
}
