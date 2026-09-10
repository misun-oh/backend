package com.example.hr.service;

import com.example.hr.dto.LoginMember;

public interface LoginService {

    /** 인증 성공 시 로그인 정보, 실패 시 null */
    LoginMember login(String username, String password);
}
