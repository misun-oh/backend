package com.example.hr.dto;

/**
 * 부록 2. JWT — 로그인/재발급 응답.
 */
public record TokenResponse(String accessToken, String refreshToken) {
}
