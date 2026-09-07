package com.example.hr.controller;

import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpForm;
import com.example.hr.service.DeptService;
import com.example.hr.service.EmpService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Day 10 — 컨트롤러가 Model에 데이터를 담아 뷰로 넘기는 예제.
 * 본문 2절의 list()에 실습/실습답안에서 만든 등록·수정 화면 메서드를 더한 병합본.
 */
@Controller
public class EmpViewController {

    private final EmpService empService;   // Day 9에서 만든 서비스
    private final DeptService deptService;

    public EmpViewController(EmpService empService, DeptService deptService) {
        this.empService = empService;
        this.deptService = deptService;
    }

    @GetMapping("/emps")
    public String list(Model model) {
        model.addAttribute("emps", empService.findAll());   // 진짜 DB 데이터
        model.addAttribute("title", "사원 목록");
        return "hr/index";
    }

    // 실습 문제 4 — 등록 폼
    @GetMapping("/emps/new")
    public String newForm(Model model) {
        model.addAttribute("form", new EmpForm());
        model.addAttribute("depts", deptService.findAll());
        return "emp/form";
    }

    // 실습답안 문제 4 & 5 — 수정 화면(등록 폼과 같은 emp/form.html 재사용)
    @GetMapping("/emps/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Emp e = empService.get(id);
        EmpForm form = new EmpForm();
        form.setEmpId(e.getEmpId());     // EmpForm 에 empId 필드 추가 필요
        form.setEmpName(e.getEmpName());
        form.setEmail(e.getEmail());
        form.setDeptId(e.getDeptId());
        form.setSalary(e.getSalary());
        form.setHireDate(e.getHireDate());
        model.addAttribute("form", form);
        model.addAttribute("depts", deptService.findAll());
        return "emp/form";
    }

    @PostMapping("/emps/{id}")
    public String update(@PathVariable Long id, @ModelAttribute EmpForm form, RedirectAttributes ra) {
        empService.modify(id, form);
        ra.addFlashAttribute("message", "수정되었습니다");
        return "redirect:/emps/" + id;
    }
}
