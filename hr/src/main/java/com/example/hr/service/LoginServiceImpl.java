package com.example.hr.service;

import org.springframework.stereotype.Service;

import com.example.hr.dto.LoginMember;

/**
 * 인사담당자 로그인 — 실무에서는 회원(Member) 테이블 + 암호화된 비밀번호로 인증한다.
 * 이 과정은 Day 12(로그인과 권한)에서 다루므로, 지금은 R6("로그인한 담당자만")을
 * 만족시키는 최소 구현으로 고정 계정 하나만 둔다.
 */
@Service
public class LoginServiceImpl implements LoginService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "1234";
    private static final String ADMIN_NAME = "인사담당자";

    @Override
    public LoginMember login(String username, String password) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            return new LoginMember(username, ADMIN_NAME);
        }
        return null;
    }
}
