package com.example.hr.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import lombok.Getter;
import lombok.Setter;

/** 사원 등록·수정 폼 — emp-form.html 과 1:1. */
@Getter
@Setter
public class EmpForm {

    private Long empId;

    @NotBlank(message = "이름을 입력하세요.")
    private String empName;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다. (예: hong@company.com)")
    private String email;

    private String phone;

    @NotNull(message = "부서를 선택하세요.")
    private Long deptId;

    @NotBlank(message = "직급을 선택하세요.")
    private String jobCode;

    @NotNull(message = "기본급을 입력하세요.")
    @Min(value = 0, message = "급여는 0 이상이어야 합니다.")
    private Integer salary;

    private Long managerId;

    @NotNull(message = "입사일을 입력하세요.")
    @PastOrPresent(message = "입사일은 오늘 이전이어야 합니다.")
    private LocalDate hireDate;

    private boolean active = true;

    private String memo;

    public boolean isNew() {
        return empId == null;
    }
}
