package com.example.hr.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.example.hr.domain.Emp;
import com.example.hr.mapper.EmpMapper;

/** Day 7 확인용 임시 컨트롤러 — EmpMapper 를 직접 호출해 결과를 확인한다. */
@RestController
@RequiredArgsConstructor
public class RawEmpController {
    private final EmpMapper empMapper;

    @GetMapping("/raw/emps")
    List<Emp> all() { return empMapper.findAll(); }

    @GetMapping("/raw/emps/{id}")
    Emp one(@PathVariable Long id) { return empMapper.findById(id); }
}
