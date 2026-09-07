package com.example.hr.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Day 4 실습답안 문제3 — 같은 타입(Notifier) 빈이 여러 개일 때 기본으로 선택되는 구현.
 */
@Primary
@Component
public class SmsNotifier implements Notifier {
    @Override
    public String send(String msg) {
        return "[sms] " + msg;
    }
}
