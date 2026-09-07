package com.example.hr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Day 2 실습답안 — 기동 시 메시지 출력, 방법 B (별도 컴포넌트).
 * HrApplication 의 @Bean CommandLineRunner(방법 A)와 같은 목적의 대안 구현.
 * 둘 다 컨테이너가 준비되고 톰캣이 요청을 받기 직전에 한 번 실행된다.
 */
@Component
public class StartupBanner implements CommandLineRunner {

    @Value("${server.port}")
    private String port;

    @Override
    public void run(String... args) {
        System.out.println(">>> HR 시스템 준비 완료 (포트: " + port + ")");
    }
}
