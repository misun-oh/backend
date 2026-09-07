package com.example.hr.common;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Day 4 실습답안 문제5 — 생명주기 콜백. @PostConstruct 는 생성자 실행 + 의존성 주입이
 * 끝난 뒤 호출되고, @PreDestroy 는 컨테이너 종료 직전에 호출된다.
 */
@Slf4j
@Component
public class StartupChecker {

    @PostConstruct
    public void init() {
        log.info("의존성 주입 완료");
    }

    @PreDestroy
    public void close() {
        log.info("종료 정리");
    }
}
