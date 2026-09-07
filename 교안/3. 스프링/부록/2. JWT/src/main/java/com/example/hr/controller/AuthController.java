package com.example.hr.controller;

import com.example.hr.dto.LoginRequest;
import com.example.hr.dto.RefreshRequest;
import com.example.hr.dto.TokenResponse;
import com.example.hr.security.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 부록 2. JWT — /api/auth/login (+ 문제 5의 /api/auth/refresh 최소 구현).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;   // @Bean 으로 노출 필요
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();   // ["ROLE_ADMIN"]

        String access = jwtProvider.createAccessToken(auth.getName(), roles);
        // refresh 토큰은 별도 저장(DB/Redis) 권장 — 여기선 개념만
        return new TokenResponse(access, /* refresh */ null);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest req) {
        Claims c = jwtProvider.parse(req.refreshToken()).getPayload();   // 서명·만료 검증
        // (실무) 저장소에서 이 refresh 가 유효한지 조회 + 회전(새 refresh 발급, 기존 폐기)
        List<String> roles = c.get("roles", List.class);
        String access = jwtProvider.createAccessToken(c.getSubject(), roles);
        return new TokenResponse(access, req.refreshToken());
    }
}
