package com.example.hr.service;

import java.time.Clock;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Day 4 실습답안 문제1+2 — 생성자 주입으로 Clock 빈을 받아 인사말에 현재 시각을 붙인다.
 * 테스트에서는 new GreetingService(Clock.fixed(instant, zone)) 로 시각을 고정할 수 있다.
 */
@Service
@RequiredArgsConstructor
public class GreetingService {

    private final Clock clock;

    public String greet(String name) {
        LocalTime now = LocalTime.now(clock);
        return "환영합니다, %s님 (%02d:%02d)".formatted(name, now.getHour(), now.getMinute());
    }
}
