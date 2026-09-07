package com.example.hr.controller;

import com.example.hr.dto.SignupForm;
import com.example.hr.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 부록 3. 로그인 화면 + 회원가입.
 * 로그인 처리(POST /login)는 시큐리티(SecurityConfig)가 가로채므로 우리 메서드 불필요.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signup(Model m) {
        m.addAttribute("form", new SignupForm(null, null, null));
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String doSignup(@Valid @ModelAttribute("form") SignupForm form, BindingResult br) {
        if (br.hasErrors()) return "auth/signup";
        memberService.signup(form);
        return "redirect:/login?signup";
    }
}
