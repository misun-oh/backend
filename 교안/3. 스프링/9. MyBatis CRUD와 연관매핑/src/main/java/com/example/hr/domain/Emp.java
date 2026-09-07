package com.example.hr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 사원 도메인.
 * deptName/jobName 은 목록 조회(findList)에서 조인으로 채워지는 읽기 전용 필드,
 * dept 는 상세 조회(findByIdWithDept, <association>)에서 채워지는 중첩 부서 객체.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Emp {

    private Long empId;
    private String empName;
    private String email;
    private String phone;
    private Long deptId;
    private String jobCode;
    private int salary;
    private LocalDate hireDate;
    private boolean active;

    // 목록 조인용 읽기 전용 필드 (findList: d.DEPT_TITLE AS deptName, j.JOB_NAME AS jobName)
    private String deptName;
    private String jobName;

    // 상세 조인용 중첩 객체 (findByIdWithDept, <association property="dept">)
    private Dept dept;
}
