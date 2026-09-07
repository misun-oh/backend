package com.example.hr.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpForm;
import com.example.hr.repository.EmpRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpServiceImpl implements EmpService {

    private final EmpRepository empRepository;

    @Override
    public List<Emp> findAll() {
        return empRepository.findAll();      // 부서명(deptName) 조인 채움은 Day 9
    }

    @Override
    public Emp get(Long id) {
        return empRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("사원 없음: " + id));
    }

    @Override
    public Long register(EmpForm form) {
        Emp emp = Emp.builder()
                .empName(form.getEmpName()).email(form.getEmail())
                .deptId(form.getDeptId())
                .salary(form.getSalary() == null ? 0 : form.getSalary())
                .hireDate(form.getHireDate()).active(true)
                .build();
        Emp saved = empRepository.save(emp);
        log.info("사원 등록: empId={}", saved.getEmpId());
        return saved.getEmpId();
    }

    @Override
    public void modify(Long id, EmpForm form) {
        Emp cur = get(id);
        empRepository.save(Emp.builder()
                .empId(cur.getEmpId())
                .empName(form.getEmpName()).email(form.getEmail())
                .deptId(form.getDeptId())
                .salary(form.getSalary() == null ? cur.getSalary() : form.getSalary())
                .hireDate(form.getHireDate()).active(cur.isActive())
                .build());
    }

    @Override
    public void remove(Long id) {
        get(id);
        empRepository.deleteById(id);
    }
}
