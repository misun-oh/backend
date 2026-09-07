package com.example.hr.common;

import com.example.hr.domain.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Day 12 — 본문 7절. 모든 뷰에 로그인 사용자 정보를 공통으로 넘겨준다.
 * 세션에 이미 있는 값을 꺼내기만 하므로 DB 조회는 하지 않는다(심화 5절).
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("loginMember")
    public Member loginMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (Member) session.getAttribute("loginMember") : null;
    }
}
