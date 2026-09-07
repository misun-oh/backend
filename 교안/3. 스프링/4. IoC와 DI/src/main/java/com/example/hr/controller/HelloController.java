package com.example.hr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hr.service.GreetingService;

import lombok.RequiredArgsConstructor;

/**
 * Day 4 실습답안 문제1 — GreetingService 를 생성자 주입받아 사용한다.
 */
@RestController
@RequiredArgsConstructor
public class HelloController {

    private final GreetingService greetingService;   // final → 생성자 주입 대상

    @GetMapping("/welcome")
    public String welcome(@RequestParam String name) {
        return greetingService.greet(name);
    }
}
