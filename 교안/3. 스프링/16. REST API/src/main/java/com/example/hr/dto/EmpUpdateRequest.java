package com.example.hr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record EmpUpdateRequest(
        @NotBlank String empName, @NotBlank @Email String email,
        @NotNull Long deptId, @NotNull @PositiveOrZero Integer salary,
        @NotNull @PastOrPresent LocalDate hireDate) {

    public EmpForm toForm() {
        // 실습 답안: "동일" — EmpCreateRequest.toForm() 과 동일한 매핑
        EmpForm f = new EmpForm();
        f.setEmpName(empName); f.setEmail(email); f.setDeptId(deptId);
        f.setSalary(salary); f.setHireDate(hireDate);
        return f;
    }
}
