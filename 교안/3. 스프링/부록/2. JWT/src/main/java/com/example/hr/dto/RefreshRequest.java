package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 부록 2. JWT — 문제 5(재발급)의 /api/auth/refresh 요청 바디.
 */
public record RefreshRequest(@NotBlank String refreshToken) {
}
