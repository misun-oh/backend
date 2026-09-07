package com.example.hr.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpForm;
import com.example.hr.service.EmpService;

@RestController
@RequestMapping("/api/emps")
@RequiredArgsConstructor
public class EmpController {

    private final EmpService empService;

    @GetMapping
    public List<Emp> list() { return empService.findAll(); }

    @GetMapping("/{id}")
    public Emp detail(@PathVariable Long id) { return empService.get(id); }

    @PostMapping
    public Map<String, Long> create(@RequestBody EmpForm form) {
        return Map.of("empId", empService.register(form));
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody EmpForm form) {
        empService.modify(id, form);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { empService.remove(id); }
}
