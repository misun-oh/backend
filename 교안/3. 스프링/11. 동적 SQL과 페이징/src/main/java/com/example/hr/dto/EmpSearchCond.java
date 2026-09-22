package com.example.hr.dto;

import lombok.Data;

/**
 * Day 11 — 검색 조건 + 페이지 요청 정보 DTO.
 * keyword/deptId/workingOnly 는 1_동적SQL과검색.md, sort/page/size 는 2_페이징.md 에서 추가.
 */
@Data
public class EmpSearchCond {
    private String keyword;
    private String deptId;
    private boolean workingOnly;
    private EmpSort sort = EmpSort.EMP_ID;

    private int page = 1;   // 1부터
    private int size = 10;

    public int getOffset() {
        return (Math.max(page, 1) - 1) * size;
    }
}
