package com.example.hr.dto;

import java.util.List;

/**
 * Day 11 — 페이징 결과 래퍼. 실습답안의 totalPages() size==0 방어 포함(더 완전한 버전).
 */
public record PageResult<T>(List<T> content, long totalElements, int page, int size) {

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean hasPrev() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages();
    }
}
