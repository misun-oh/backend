package com.example.hr.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

/**
 * 부록 2. JWT — jjwt 기반 토큰 생성/검증 유틸.
 * secret 은 32바이트(HS256) 이상이어야 한다.
 */
@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessExpMs;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.access-exp-min:30}") long accessExpMin) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // 32바이트 이상
        this.accessExpMs = accessExpMin * 60_000;
    }

    public String createAccessToken(String memberId, Collection<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(memberId)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpMs)))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) {          // 서명·만료 검증 포함, 실패 시 예외
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}
