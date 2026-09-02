package com.example.hr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Day 2 — 화면(HTML)을 돌려주는 컨트롤러 + hr 프로토타입 연결.
 *
 * @RestController 는 반환 문자열을 "응답 본문 그대로" 쓰지만,
 * @Controller 는 반환 문자열을 "뷰 이름"으로 해석한다.
 *   - "redirect:/..." : 뷰가 아니라 다른 URL로 이동
 *   - "greeting"      : src/main/resources/templates/greeting.html 을 렌더링
 *
 * 아래 매핑은 Day 1 설계의 URL 표 그대로다. 오늘은 데이터가 없으므로,
 * 각 주소를 static/hr/*.html 프로토타입 화면으로 redirect 만 한다.
 * Day 7 에서 redirect 대신 Model + 뷰 이름(hr/xxx)으로 실제 데이터를 렌더링한다.
 */
@Controller
public class HomeController {

    /* -------- 대시보드 -------- */

    // GET /  → 대시보드
    @GetMapping("/")
    public String dashboard() {
        return "redirect:/hr/dashboard.html";
    }

    /* -------- 사원 -------- */

    // GET /emps  → 사원 목록·검색
    @GetMapping("/emps")
    public String empList() {
        return "redirect:/hr/index.html";
    }

    // GET /emps/new  → 사원 등록 폼   (구체적 경로라 /emps/{id} 보다 먼저 매칭됨)
    @GetMapping("/emps/new")
    public String empNewForm() {
        return "redirect:/hr/emp-form.html";
    }

    // GET /emps/{id}  → 사원 상세 (id 는 Day 7 에서 @PathVariable 로 실제 사용)
    @GetMapping("/emps/{id}")
    public String empDetail(@PathVariable String id) {
        return "redirect:/hr/emp-detail.html";
    }

    // GET /emps/{id}/edit  → 사원 수정 폼
    @GetMapping("/emps/{id}/edit")
    public String empEditForm(@PathVariable String id) {
        return "redirect:/hr/emp-form.html";
    }

    /* -------- 부서 -------- */

    // GET /depts  → 부서 목록
    @GetMapping("/depts")
    public String deptList() {
        return "redirect:/hr/depts.html";
    }

    /* -------- 로그인 (자리만) -------- */

    // POST /login  → 실제 인증은 Day 17(Spring Security). 지금은 대시보드로.
    @PostMapping("/login")
    public String login() {
        return "redirect:/";
    }

    /* -------- @Controller + 뷰 이름 예시 -------- */

    // GET /greeting  → templates/greeting.html 렌더링
    @GetMapping("/greeting")
    public String greeting() {
        return "greeting";
    }
}
