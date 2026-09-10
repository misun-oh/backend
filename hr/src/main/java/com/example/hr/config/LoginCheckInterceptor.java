package com.example.hr.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;

import com.example.hr.common.SessionConst;

/** R6 — 로그인한 담당자만 사원·부서·대시보드 기능을 쓸 수 있도록 막는다. */
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Object loginMember = session == null ? null : session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            String requestURI = request.getRequestURI();
            response.sendRedirect("/login?redirectURL=" + requestURI);
            return false;
        }
        request.setAttribute("loginMember", loginMember);
        return true;
    }
}
