package com.example.hr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

// 요청: 클라이언트가 보낼 수 있는 것만
public record EmpCreateRequest(
        @NotBlank String empName,
        @NotBlank @Email String email,
        @NotNull Long deptId,
        @NotNull @PositiveOrZero Integer salary,
        @NotNull @PastOrPresent LocalDate hireDate) {

    public EmpForm toForm() {
        EmpForm f = new EmpForm();
        f.setEmpName(empName); f.setEmail(email); f.setDeptId(deptId);
        f.setSalary(salary); f.setHireDate(hireDate);
        return f;
    }
}
