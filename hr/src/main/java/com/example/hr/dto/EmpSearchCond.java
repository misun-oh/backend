package com.example.hr.dto;

import lombok.Getter;
import lombok.Setter;

/** 사원 목록 검색 조건 — GET /emps 쿼리스트링과 1:1. */
@Getter
@Setter
public class EmpSearchCond {

    public static final int PAGE_SIZE = 10;

    /** 이름·이메일 검색어 */
    private String keyword;
    private Long deptId;
    /** 재직자만 볼지 여부 (체크박스, 기본 true) */
    private boolean workingOnly = true;
    /** hireDate | name | salary */
    private String sort = "hireDate";
    private int page = 1;

    public int getOffset() {
        int p = Math.max(page, 1);
        return (p - 1) * PAGE_SIZE;
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }
}
