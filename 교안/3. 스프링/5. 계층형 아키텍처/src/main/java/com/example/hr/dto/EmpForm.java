package com.example.hr.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmpForm {
    private String empName;
    private String email;
    private Long deptId;
    private Integer salary;
    private LocalDate hireDate;
}
