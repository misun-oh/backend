package com.example.hr.service;

import org.springframework.stereotype.Component;

/**
 * Day 4 실습답안 문제3 — AlertService 가 @Qualifier("emailNotifier") 로 명시적으로 지정해 쓴다.
 */
@Component
public class EmailNotifier implements Notifier {
    @Override
    public String send(String msg) {
        return "[email] " + msg;
    }
}
