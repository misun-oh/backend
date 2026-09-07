package com.example.hr.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Day 4 실습답안 문제3 — 생성자 파라미터에 직접 @Qualifier 를 붙이는 권장 형태.
 * emailNotifier 는 이메일을 명시적으로 지정해서 받고, allNotifiers 는 Notifier 타입 빈 전부를 받는다.
 */
@Service
public class AlertService {

    private final Notifier emailNotifier;
    private final List<Notifier> allNotifiers;

    public AlertService(@Qualifier("emailNotifier") Notifier emailNotifier,
                         List<Notifier> allNotifiers) {
        this.emailNotifier = emailNotifier;
        this.allNotifiers = allNotifiers;
    }

    public String alert(String msg) {
        return emailNotifier.send(msg);
    }

    public List<String> alertAll(String msg) {
        return allNotifiers.stream().map(n -> n.send(msg)).toList();
    }
}
