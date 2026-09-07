package com.example.hr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day 3 실습답안 — @Slf4j 로 레벨별 로그를 찍고, 예외 발생 시 스택트레이스까지 남긴다.
 */
@Slf4j
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "손님") String name) {
        log.trace("trace: name={}", name);
        log.debug("debug: name={}", name);
        log.info("info: name={}", name);
        log.warn("warn: name={}", name);
        log.error("error: name={}", name);
        return "안녕하세요, " + name + "님";
    }

    @GetMapping("/divide")
    public String divide(@RequestParam int a, @RequestParam int b) {
        try {
            int result = a / b;
            log.info("나눗셈 성공: {} / {} = {}", a, b, result);
            return String.valueOf(result);
        } catch (ArithmeticException e) {
            log.error("나눗셈 실패: a={}, b={}", a, b, e);   // 마지막 인자 e → 스택트레이스 출력
            return "0으로 나눌 수 없습니다";
        }
    }
}
