package com.example.hr.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.example.hr.domain.Dept;
import com.example.hr.dto.DeptForm;
import com.example.hr.service.DeptService;

@RestController
@RequestMapping("/api/depts")
@RequiredArgsConstructor
public class DeptController {
    private final DeptService deptService;

    @GetMapping
    public List<Dept> list() { return deptService.findAll(); }

    @PostMapping
    public Map<String, Long> create(@RequestBody DeptForm f) {
        return Map.of("deptId", deptService.register(f));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { deptService.remove(id); }
}
