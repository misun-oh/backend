package com.example.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 부서 목록 한 줄 = 부서 + 소속 인원수 + 평균 급여.
 * memberCount·avgSalary 는 EMP 를 집계(GROUP BY)해야 나오는 화면 전용 값이라
 * Dept 도메인에 얹지 않고 전용 DTO 로 뺀다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeptListRow {

    private Long deptId;
    private String deptName;
    private String location;
    private int memberCount;
    private long avgSalary;

    public String getAvgSalaryDisplay() {
        return memberCount == 0 ? "-" : String.format("%,d원", avgSalary);
    }
}
