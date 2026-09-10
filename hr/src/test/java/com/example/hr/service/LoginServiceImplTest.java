package com.example.hr.service;

import org.junit.jupiter.api.Test;

import com.example.hr.dto.LoginMember;

import static org.assertj.core.api.Assertions.assertThat;

class LoginServiceImplTest {

    LoginService loginService = new LoginServiceImpl();

    @Test
    void 아이디_비밀번호가_맞으면_로그인_정보를_반환한다() {
        LoginMember member = loginService.login("admin", "1234");

        assertThat(member).isNotNull();
        assertThat(member.getUsername()).isEqualTo("admin");
    }

    @Test
    void 비밀번호가_틀리면_null을_반환한다() {
        assertThat(loginService.login("admin", "wrong")).isNull();
    }

    @Test
    void 아이디가_틀리면_null을_반환한다() {
        assertThat(loginService.login("nobody", "1234")).isNull();
    }
}
