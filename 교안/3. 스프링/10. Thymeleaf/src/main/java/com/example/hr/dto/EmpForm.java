package com.example.hr.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Day 10 예제코드 — dto/EmpForm.java (empId 필드 추가)
 */
@Getter
@Setter
public class EmpForm {
    private Long empId;          // 수정 화면 재사용을 위해 추가 (등록 시 null)
    private String empName;
    private String email;
    private Long deptId;
    private Integer salary;
    private LocalDate hireDate;
}
