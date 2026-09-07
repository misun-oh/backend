package com.example.hr.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * 부록 4. 심화 — 로그인 성공/실패 이벤트 감사 로그.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventListener {

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent e) {
        log.info("로그인 성공: {}", e.getAuthentication().getName());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent e) {
        log.warn("로그인 실패: {} ({})", e.getAuthentication().getName(), e.getException().getMessage());
    }
}
