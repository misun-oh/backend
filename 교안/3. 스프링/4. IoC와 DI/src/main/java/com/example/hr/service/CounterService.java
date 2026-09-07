package com.example.hr.service;

import org.springframework.stereotype.Service;

/**
 * Day 4 실습답안 문제4 — 일부러 만든 상태 버그 예시.
 * 싱글턴 빈에 가변 상태(count)를 두면 동시 요청이 같은 필드를 함께 증가시켜 값이 섞인다
 * (++count 도 원자적이지 않아 경쟁 조건까지 생긴다). "요청마다 1"이 목적이면 필드에 상태를 두지 않거나,
 * 진짜 누적이 필요하면 AtomicInteger 또는 @Scope("request") 를 검토한다.
 */
@Service
public class CounterService {
    private int count;

    public int increment() {
        return ++count;
    }
}
