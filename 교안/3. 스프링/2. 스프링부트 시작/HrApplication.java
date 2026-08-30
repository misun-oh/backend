package com.example.hr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Day 2 — 스프링 부트 시작
 * @SpringBootApplication = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan
 * 이 클래스는 반드시 기준 패키지(com.example.hr) 최상단에 있어야 컴포넌트 스캔이 하위 전체를 잡는다.
 */
@SpringBootApplication
public class HrApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrApplication.class, args);
    }

    /** 기동 직후 한 번 실행 (실무에서는 System.out 대신 로거 — Day 3) */
    @Bean
    CommandLineRunner startupBanner(@Value("${server.port:8080}") String port) {
        return args -> System.out.println(">>> HR 시스템 준비 완료 (포트: " + port + ")");
    }
}
