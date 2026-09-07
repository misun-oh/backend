package com.example.hr.mapper;

import com.example.hr.domain.Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmpMapper {
    List<Emp> findAll();
    Emp findById(Long empId);
    int insert(Emp emp);            // 반환값 = 영향 받은 행 수
    int update(Emp emp);
    int deleteById(Long empId);
    List<Emp> findList();               // 목록: DEPT·JOB 조인해 deptName·jobName 채움
    Emp findByIdWithDept(Long empId);
    int updateSalary(@Param("empId") Long empId, @Param("salary") int salary);
}
