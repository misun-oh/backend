package com.example.hr.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;

import com.example.hr.common.SessionConst;
import com.example.hr.dto.LoginForm;
import com.example.hr.dto.LoginMember;
import com.example.hr.service.LoginService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm loginForm) {
        return "hr/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                         BindingResult bindingResult,
                         @RequestParam(defaultValue = "/") String redirectURL,
                         HttpServletRequest request,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "hr/login";
        }

        LoginMember loginMember = loginService.login(loginForm.getUsername(), loginForm.getPassword());
        if (loginMember == null) {
            model.addAttribute("loginError", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "hr/login";
        }

        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
        return "redirect:" + safeRedirectURL(redirectURL);
    }

    /** 오픈 리다이렉트 방지 — 같은 서버의 경로("/..")만 허용한다. */
    private String safeRedirectURL(String redirectURL) {
        if (redirectURL == null || !redirectURL.startsWith("/") || redirectURL.startsWith("//")) {
            return "/";
        }
        return redirectURL;
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
