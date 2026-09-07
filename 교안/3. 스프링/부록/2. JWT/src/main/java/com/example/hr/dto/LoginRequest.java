package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 부록 2. JWT — 로그인 요청. username 은 우리 MEMBER_ID(로그인 아이디)를 담는다.
 */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}
