package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Day 2 — 사원 관리 "화면(HTML)" 컨트롤러.
 *
 * <p>{@code @Controller} 는 핸들러가 돌려준 문자열을 <b>뷰 이름</b>으로 본다.
 * 접두어({@code redirect:} / {@code forward:}) 를 붙이지 않으면 그 문자열이
 * 그대로 뷰 이름이 되고, ViewResolver(여기서는 Thymeleaf) 설정에 따라
 * <pre>  classpath:/templates/ + &lt;뷰 이름&gt; + .html  </pre>
 * 파일을 찾아 서버가 렌더링해서 응답한다. (브라우저 주소창은 안 바뀌고, 상태코드 200)
 *
 * <p>예) {@code return "hr/index"} → {@code templates/hr/index.html} 렌더링
 *
 * <p>매핑은 Day 1 설계의 URL 표 그대로다. 지금은 데이터가 없어 정적 화면만
 * 띄우고, Day 7 에서 {@code Model} 에 값을 담아 <b>같은 뷰 이름</b>을 그대로
 * 렌더링하도록 바꾼다. 부서·대시보드 화면도 당장은 여기 함께 두고, 화면이
 * 늘어나면 {@code DeptController} 등으로 분리한다.
 */
@Controller
public class EmpController {

    /** GET / — 대시보드(요약 통계) 화면을 보여준다. → templates/hr/dashboard.html */
    @GetMapping("/")
    public String dashboard() {
        return "hr/dashboard";
    }

    /** GET /emps — 사원 목록·검색 화면을 보여준다. → templates/hr/index.html */
    @GetMapping("/emps")
    public String empList() {
        return "hr/index";
    }

    /**
     * GET /emps/new — 사원 등록 폼 화면을 보여준다. → templates/hr/emp-form.html
     * <p>리터럴 경로라서 아래 {@code /emps/{id}} 보다 먼저 매칭된다.
     */
    @GetMapping("/emps/new")
    public String empNewForm() {
        return "hr/emp-form";
    }

    /**
     * GET /emps/{id} — 사원 상세 화면을 보여준다. → templates/hr/emp-detail.html
     * <p>{@code id} 는 Day 7 에서 조회 조건으로 쓴다. 지금은 받기만 한다.
     */
    @GetMapping("/emps/{id}")
    public String empDetail(@PathVariable String id) {
        return "hr/emp-detail";
    }

    /**
     * GET /emps/{id}/edit — 사원 수정 폼 화면을 보여준다.
     * <p>등록 폼과 같은 템플릿을 재사용한다. → templates/hr/emp-form.html
     */
    @GetMapping("/emps/{id}/edit")
    public String empEditForm(@PathVariable String id) {
        return "hr/emp-form";
    }

    /** GET /depts — 부서 목록 화면을 보여준다. → templates/hr/depts.html */
    @GetMapping("/depts")
    public String deptList() {
        return "hr/depts";
    }

    /**
     * POST /login — 로그인 처리 자리(실제 인증은 Day 17 Spring Security).
     * <p>지금은 뷰 이름만 돌려 대시보드를 곧바로 렌더링한다. 실무라면 처리 후
     * {@code redirect:/} 로 PRG(Post-Redirect-Get) 패턴을 쓰지만, 여기서는
     * "뷰 이름 → ViewResolver → 화면" 흐름만 확인한다.
     */
    @PostMapping("/login")
    public String login() {
        return "hr/dashboard";
    }
}
