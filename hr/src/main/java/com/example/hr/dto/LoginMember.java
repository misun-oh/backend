package com.example.hr.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 세션에 저장하는 로그인 사용자 정보. */
@Getter
@AllArgsConstructor
public class LoginMember implements Serializable {

    private final String username;
    private final String name;
}
