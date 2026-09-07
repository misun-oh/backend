package com.example.hr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.hr.domain.Emp;

import java.time.LocalDate;

// 응답: 클라이언트에게 보여줄 것만 (내부 필드·민감정보 제외)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmpResponse(Long empId, String empName, String email,
                          String deptName,
                          @JsonFormat(pattern = "yyyy-MM-dd") LocalDate hireDate,
                          boolean active) {
    public static EmpResponse from(Emp e) {
        return new EmpResponse(e.getEmpId(), e.getEmpName(), e.getEmail(),
                e.getDeptName(),                    // 조회 SQL이 DEPT 조인해 채운 읽기 필드
                e.getHireDate(), e.isActive());
    }
}
