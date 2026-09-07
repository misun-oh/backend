package com.example.hr.common;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Day 3 심화 — MDC(Mapped Diagnostic Context)에 요청별 reqId 를 넣어,
 * 같은 요청에서 나온 로그를 한 값으로 묶어 추적할 수 있게 한다.
 * 패턴 예: "%d %-5level [%X{reqId}] %logger - %msg%n"
 */
@Component
public class RequestIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        MDC.put("reqId", UUID.randomUUID().toString().substring(0, 8));
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();   // 스레드 재사용되므로 반드시 정리
        }
    }
}
