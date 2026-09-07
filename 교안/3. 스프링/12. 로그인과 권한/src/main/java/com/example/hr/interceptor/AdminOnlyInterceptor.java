package com.example.hr.interceptor;

import com.example.hr.domain.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Day 12 — 본문 6절. "ADMIN만 접근 가능한 경로"를 좁게 통제하는 두 번째 인터셉터.
 * WebConfig 에서 addPathPatterns("/depts/**", "/emps/*/delete") 로 등록.
 * 항상 LoginCheckInterceptor 다음 순서로 등록해야 한다(자주 하는 실수 참고).
 */
public class AdminOnlyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        Member member = session != null ? (Member) session.getAttribute("loginMember") : null;

        if (member == null || !"ADMIN".equals(member.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자만 접근할 수 있습니다.");
            return false;
        }
        return true;
    }
}
