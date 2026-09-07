package com.example.hr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 부서 도메인.
 * emps 는 부서 상세에서 소속 사원 목록을 함께 조회할 때(<collection property="emps">) 채워지는 필드.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dept {

    private Long deptId;
    private String deptName;
    private String location;

    private List<Emp> emps;
}
