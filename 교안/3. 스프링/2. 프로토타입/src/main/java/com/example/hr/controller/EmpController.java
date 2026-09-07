package com.example.hr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Day 2 프로토타입 — hr 프로토타입 화면(templates/hr/*.html)을 Day 1 설계 URL 에 연결한다.
 * 파일이 이미 templates/ 안에 있으므로 redirect: 없이 뷰 이름을 그대로 반환한다.
 * {id}는 오늘은 사용하지 않는다 — 실제 조회는 Day 6에서 @PathVariable로 이어진다.
 */
@Controller
public class EmpController {

    @GetMapping("/")                 public String dashboard()   { return "hr/dashboard"; }

    @GetMapping("/emps")             public String empList()      { return "hr/index"; }
    @GetMapping("/emps/new")         public String empNewForm()   { return "hr/emp-form"; }   // {id} 보다 먼저 매칭
    @GetMapping("/emps/{id}")        public String empDetail(@PathVariable String id)   { return "hr/emp-detail"; }
    @GetMapping("/emps/{id}/edit")   public String empEditForm(@PathVariable String id) { return "hr/emp-form"; }

    @GetMapping("/depts")            public String deptList()     { return "hr/depts"; }

    @PostMapping("/login")           public String login()        { return "hr/dashboard"; }   // 실제 인증은 Day 12
}
