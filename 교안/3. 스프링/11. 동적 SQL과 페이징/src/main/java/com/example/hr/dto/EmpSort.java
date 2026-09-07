package com.example.hr.dto;

/**
 * Day 11 — 정렬 화이트리스트. ORDER BY 는 ${} 로 SQL 조각을 그대로 넣어야 하므로
 * 임의 문자열이 들어오지 못하도록 enum 상수로만 제한한다(본문 6절, 컬럼에 별칭 e. 접두 유지).
 */
public enum EmpSort {
    EMP_ID("e.EMP_ID", "ASC"),
    HIRE_DATE("e.HIRE_DATE", "DESC"),
    SALARY("e.SALARY", "DESC"),
    NAME("e.EMP_NAME", "ASC");

    public final String column;
    public final String direction;

    EmpSort(String c, String d) {
        this.column = c;
        this.direction = d;
    }
}
