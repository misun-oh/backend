package com.example.hr.dto;

/**
 * Day 11-2 — 정렬 화이트리스트. ORDER BY 는 ${} 로 SQL 조각을 그대로 넣어야 하므로
 * 임의 문자열이 들어오지 못하도록 enum 상수로만 제한한다(2_페이징.md 4절).
 */
public enum EmpSort {
    EMP_ID("emp_id", "asc"),
    HIRE_DATE("hire_date", "desc"),
    SALARY("salary", "desc"),
    NAME("emp_name", "asc");

    private final String column;
    private final String direction;

    EmpSort(String column, String direction) {
        this.column = column;
        this.direction = direction;
    }

    public String getColumn() { return column; }
    public String getDirection() { return direction; }
}
