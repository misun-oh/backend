package com.example.hr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 부서 도메인 — DEPT 테이블과 1:1. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dept {

    private Long deptId;
    private String deptName;
    private String location;
}
