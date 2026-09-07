package com.example.hr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.hr.dto.EmpForm;
import com.example.hr.service.EmpService;

@Slf4j
@Controller
@RequestMapping("/emps")
@RequiredArgsConstructor
public class EmpViewController {

    private final EmpService empService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        log.info("목록 요청: keyword={}, page={}", keyword, page);
        model.addAttribute("emps", empService.findAll());
        return "emp/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("emp", empService.get(id));
        return "emp/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new EmpForm());
        return "emp/form";
    }

    @PostMapping
    public String create(@ModelAttribute EmpForm form, RedirectAttributes ra) {
        Long id = empService.register(form);
        ra.addFlashAttribute("message", "등록되었습니다");
        return "redirect:/emps/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        empService.remove(id);
        ra.addFlashAttribute("message", "삭제되었습니다");
        return "redirect:/emps";
    }
}
