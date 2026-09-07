package com.example.hr.controller;

import com.example.hr.domain.Member;
import com.example.hr.mapper.MemberMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Day 12 — 본문 4절. 로그인/로그아웃 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final MemberMapper memberMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String memberId, @RequestParam String password,
                         HttpServletRequest request, Model model) {
        Member member = memberMapper.findById(memberId);
        if (member == null || !encoder.matches(password, member.getPassword())) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "login";
        }
        request.getSession().setAttribute("loginMember", member);
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login";
    }
}
