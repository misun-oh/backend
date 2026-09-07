package com.example.hr.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Day 12 — 본문 5절. 페이지마다 로그인 체크를 반복하지 않도록 하는 인터셉터.
 * WebConfig 에서 excludePathPatterns("/login", "/logout", "/css/**", "/js/**", "/images/**") 로 등록.
 */
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws java.io.IOException {
        HttpSession session = request.getSession(false);   // 없으면 새로 만들지 않음
        if (session == null || session.getAttribute("loginMember") == null) {
            response.sendRedirect("/login");
            return false;   // false = 컨트롤러 호출 안 하고 여기서 끝
        }
        return true;
    }
}
