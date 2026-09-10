package com.example.hr.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사원 도메인 — EMP 테이블과 1:1.
 * deptName·jobName·managerName 은 목록·상세 조회 SQL이 DEPT·JOB·EMP(자기조인)를
 * 조인해 채우는 읽기 전용 필드다(등록·수정 시에는 비어 있음).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emp {

    private Long empId;
    private String empName;
    private String email;
    private String phone;
    private Long deptId;
    private String jobCode;
    private int salary;
    private Long managerId;
    private LocalDate hireDate;
    private boolean active;
    private String memo;

    // 조인 전용 읽기 필드
    private String deptName;
    private String jobName;
    private String managerName;

    public String getDeptNameOrUnassigned() {
        return deptName != null ? deptName : "미배정";
    }

    /** 관리자 선택 드롭다운(emp-form.html)에 쓰는 표시용 라벨. */
    public String getManagerOptionLabel() {
        return empName + " (" + getDeptNameOrUnassigned() + " " + jobName + ")";
    }
}
