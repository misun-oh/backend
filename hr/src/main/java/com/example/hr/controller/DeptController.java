package com.example.hr.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hr.common.exception.DeptInUseException;
import com.example.hr.dto.DeptForm;
import com.example.hr.service.DeptService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DeptController {

    private static final String[] LOCATIONS = {"서울", "일본", "중국", "미국", "러시아"};

    private final DeptService deptService;

    @GetMapping("/depts")
    public String list(Model model) {
        model.addAttribute("depts", deptService.listWithStats());
        model.addAttribute("locations", LOCATIONS);
        if (!model.containsAttribute("deptForm")) {
            model.addAttribute("deptForm", new DeptForm());
        }
        return "hr/depts";
    }

    @PostMapping("/depts")
    public String save(@Valid @ModelAttribute("deptForm") DeptForm form,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("openAddModal", true);
            return list(model);
        }
        deptService.register(form);
        redirectAttributes.addFlashAttribute("msg", "부서가 추가되었습니다.");
        return "redirect:/depts";
    }

    @PostMapping("/depts/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("deptForm") DeptForm form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("openEditModalId", id);
            return list(model);
        }
        deptService.modify(id, form);
        redirectAttributes.addFlashAttribute("msg", "부서 정보가 수정되었습니다.");
        return "redirect:/depts";
    }

    @PostMapping("/depts/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            deptService.remove(id);
            redirectAttributes.addFlashAttribute("msg", "부서가 삭제되었습니다.");
        } catch (DeptInUseException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/depts";
    }
}
