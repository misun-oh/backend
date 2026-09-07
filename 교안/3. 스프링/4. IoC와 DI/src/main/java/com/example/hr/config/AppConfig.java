package com.example.hr.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Day 4 — @Bean 으로 "우리가 만들지 않은 클래스"를 빈으로 등록하는 설정.
 * @Configuration 안의 @Bean 메서드끼리 호출해도 싱글턴이 보장된다(스프링 프록시).
 */
@Configuration
public class AppConfig {

    /** 시간에 의존하는 로직을 테스트 가능하게: 테스트에선 Clock.fixed(...) 로 교체 */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
