package com.example.hr.controller;

import java.time.LocalTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day 2 — 첫 컨트롤러.
 * @RestController : 메서드 반환값을 그대로 응답 본문으로 쓴다(문자열은 text, 객체는 JSON).
 * HTML 화면을 그리는 @Controller + Thymeleaf 는 Day 7~8.
 */
@RestController
public class HelloController {

    @Value("${spring.application.name:hr}")
    private String appName;

    // GET /hello , GET /hello?name=박지민
    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "손님") String name) {
        return "안녕하세요, " + name + "님";
    }

    // GET /appname  → application.yml 의 spring.application.name 값
    @GetMapping("/appname")
    public String appName() {
        return appName;
    }

    // GET /ping  → 객체 반환 시 자동으로 JSON (자동 구성된 Jackson)
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "time", LocalTime.now().toString());
    }
}
