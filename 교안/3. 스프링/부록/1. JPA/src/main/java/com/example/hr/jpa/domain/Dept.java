package com.example.hr.jpa.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DEPT")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // AUTO_INCREMENT
    @Column(name = "DEPT_ID")
    private Long deptId;

    @Column(name = "DEPT_TITLE", nullable = false, length = 35)
    private String deptName;

    @Column(name = "LOCATION_ID", length = 2)
    private String location;

    // Dept(1) → Emp(N) : 부서 하나가 사원 여러 명을 갖는다 (양방향, 선택 — 연관관계의 주인 아님)
    @OneToMany(mappedBy = "dept")
    @Builder.Default
    private List<Emp> emps = new ArrayList<>();
}
