package com.example.hr.domain;

import java.time.LocalDate;
import lombok.*;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
public class Emp {
    private Long empId;
    private String empName;
    private String email;
    private Long deptId;
    private int salary;
    private LocalDate hireDate;
    private boolean active;
    private String deptName;   // 목록에서 DEPT 조인해 채움(Day 9). 등록·수정 땐 null
    private String jobName;    // 〃 JOB 조인
}
