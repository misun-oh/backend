package com.example.hr.jpa.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "EMP")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMP_ID")
    private Long empId;

    @Column(name = "EMP_NAME", nullable = false, length = 20)
    private String empName;

    @Column(name = "EMAIL", length = 25)
    private String email;

    @Column(name = "SALARY")
    private int salary;

    @Column(name = "HIRE_DATE")
    private LocalDate hireDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPT_ID")
    private Dept dept;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_CODE")
    private Job job;

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public void setDept(Dept dept) {
        this.dept = dept;
    }
}
