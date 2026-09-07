package com.example.hr.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Day 11 — 검색 조건 DTO. 본문/실습답안 동일.
 */
@Getter
@Setter
public class EmpSearchCond {
    private String keyword;
    private String deptId;
    private boolean activeOnly;
    private EmpSort sort = EmpSort.EMP_ID;
    private int page = 1;               // 1부터
    private int size = 10;

    public int getOffset() {
        return (Math.max(page, 1) - 1) * size;
    }
}
