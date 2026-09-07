package com.example.hr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Day 12 — 본문 2절. MEMBER 테이블에 대응하는 도메인.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    private String memberId;
    private String password;
    private String memberName;
    private String role;   // "ADMIN" / "USER"
}
