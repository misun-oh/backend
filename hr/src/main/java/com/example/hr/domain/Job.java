package com.example.hr.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 직급 도메인 — JOB 테이블과 1:1. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    private String jobCode;
    private String jobName;
}
