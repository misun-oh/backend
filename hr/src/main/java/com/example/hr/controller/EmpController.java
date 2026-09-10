package com.example.hr.controller;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpForm;
import com.example.hr.dto.EmpSearchCond;
import com.example.hr.dto.PageResult;
import com.example.hr.service.DeptService;
import com.example.hr.service.EmpService;
import com.example.hr.service.JobService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EmpController {

    private final EmpService empService;
    private final DeptService deptService;
    private final JobService jobService;

    @GetMapping("/emps")
    public String list(@ModelAttribute("cond") EmpSearchCond cond, Model model) {
        PageResult<Emp> page = empService.search(cond);
        model.addAttribute("page", page);
        model.addAttribute("depts", deptService.listAll());
        model.addAttribute("totalEmp", empService.countAll());
        model.addAttribute("activeEmp", empService.countActive());
        return "hr/index";
    }

    @GetMapping("/emps/new")
    public String newForm(Model model) {
        addFormSupport(model, null);
        model.addAttribute("empForm", new EmpForm());
        return "hr/emp-form";
    }

    @GetMapping("/emps/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Emp emp = empService.get(id);
        addFormSupport(model, id);
        model.addAttribute("empForm", toForm(emp));
        return "hr/emp-form";
    }

    @PostMapping("/emps")
    public String save(@Valid @ModelAttribute("empForm") EmpForm form,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormSupport(model, null);
            return "hr/emp-form";
        }
        Long empId = empService.register(form);
        redirectAttributes.addFlashAttribute("msg", "새 사원이 등록되었습니다.");
        return "redirect:/emps/" + empId;
    }

    @PostMapping("/emps/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("empForm") EmpForm form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormSupport(model, id);
            return "hr/emp-form";
        }
        empService.modify(id, form);
        redirectAttributes.addFlashAttribute("msg", "사원 정보가 수정되었습니다.");
        return "redirect:/emps/" + id;
    }

    @PostMapping("/emps/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Emp emp = empService.get(id);
        empService.remove(id);
        redirectAttributes.addFlashAttribute("msg", emp.getEmpName() + "(" + id + ") 사원이 삭제되었습니다.");
        return "redirect:/emps";
    }

    @GetMapping("/emps/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Emp emp = empService.get(id);
        model.addAttribute("emp", emp);
        model.addAttribute("subordinates", empService.listSubordinates(id));
        model.addAttribute("tenureYears", Period.between(emp.getHireDate(), LocalDate.now()).getYears());
        return "hr/emp-detail";
    }

    // package-private: 테스트에서 MockMvc 없이 이 메서드만 직접 호출해 검증하기 위해 private을 뺐다.
    void addFormSupport(Model model, Long excludeEmpId) {
        model.addAttribute("depts", deptService.listAll());
        model.addAttribute("jobs", jobService.listAll());
        List<Emp> managers = empService.listActiveForDropdown();
        if (excludeEmpId != null) {
            managers = managers.stream()
                    .filter(m -> !m.getEmpId().equals(excludeEmpId))
                    .collect(Collectors.toList());
        }
        model.addAttribute("managers", managers);
    }

    private EmpForm toForm(Emp emp) {
        EmpForm form = new EmpForm();
        form.setEmpId(emp.getEmpId());
        form.setEmpName(emp.getEmpName());
        form.setEmail(emp.getEmail());
        form.setPhone(emp.getPhone());
        form.setDeptId(emp.getDeptId());
        form.setJobCode(emp.getJobCode());
        form.setSalary(emp.getSalary());
        form.setManagerId(emp.getManagerId());
        form.setHireDate(emp.getHireDate());
        form.setActive(emp.isActive());
        form.setMemo(emp.getMemo());
        return form;
    }
}
