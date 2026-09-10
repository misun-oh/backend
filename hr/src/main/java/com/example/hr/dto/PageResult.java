package com.example.hr.dto;

import java.util.List;

import lombok.Getter;

/** 목록 + 전체 건수 + 페이지 정보. 화면의 페이지네이션(.pagination)을 그리는 데 쓰인다. */
@Getter
public class PageResult<T> {

    private static final int PAGE_WINDOW = 5;

    private final List<T> content;
    private final long totalElements;
    private final int page;
    private final int pageSize;

    public PageResult(List<T> content, long totalElements, int page, int pageSize) {
        this.content = content;
        this.totalElements = totalElements;
        this.page = page;
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return (int) Math.max(1, Math.ceil((double) totalElements / pageSize));
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public boolean isHasPrev() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    public int getStartPage() {
        int start = ((page - 1) / PAGE_WINDOW) * PAGE_WINDOW + 1;
        return start;
    }

    public int getEndPage() {
        return Math.min(getStartPage() + PAGE_WINDOW - 1, getTotalPages());
    }
}
