package com.example.hr.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * Day 2 — 커맨드 객체(@ModelAttribute) 예시로 소개되는 검색 조건 DTO.
 * 실제 목록 검색·페이징 구현은 Day 11(동적 SQL과 페이징)에서 이어진다.
 */
@Getter
@Setter
public class EmpSearchCond {
    private String keyword;
    private int page = 1;                                   // 필드 기본값 = 기본값 역할

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireTo;
}
