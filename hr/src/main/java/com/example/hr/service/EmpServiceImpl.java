package com.example.hr.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hr.common.exception.NotFoundException;
import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpForm;
import com.example.hr.dto.EmpSearchCond;
import com.example.hr.dto.PageResult;
import com.example.hr.mapper.EmpMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpServiceImpl implements EmpService {

    private final EmpMapper empMapper;

    @Override
    public PageResult<Emp> search(EmpSearchCond cond) {
        List<Emp> content = empMapper.selectByCond(cond);
        long totalElements = empMapper.countByCond(cond);
        return new PageResult<>(content, totalElements, cond.getPage(), cond.getPageSize());
    }

    @Override
    public Emp get(Long empId) {
        Emp emp = empMapper.selectById(empId);
        if (emp == null) {
            throw new NotFoundException("사원을 찾을 수 없습니다. (empId=" + empId + ")");
        }
        return emp;
    }

    @Override
    @Transactional
    public Long register(EmpForm form) {
        Emp emp = toEmp(form);
        empMapper.insert(emp);
        return emp.getEmpId();
    }

    @Override
    @Transactional
    public void modify(Long empId, EmpForm form) {
        get(empId); // 존재 확인
        Emp emp = toEmp(form);
        emp.setEmpId(empId);
        empMapper.update(emp);
    }

    @Override
    @Transactional
    public void remove(Long empId) {
        get(empId); // 존재 확인
        empMapper.deleteById(empId);
    }

    @Override
    public List<Emp> listActiveForDropdown() {
        return empMapper.selectActiveForDropdown();
    }

    @Override
    public List<Emp> listSubordinates(Long managerId) {
        return empMapper.selectSubordinates(managerId);
    }

    @Override
    public long countAll() {
        return empMapper.countAll();
    }

    @Override
    public long countActive() {
        return empMapper.countActive();
    }

    private Emp toEmp(EmpForm form) {
        return Emp.builder()
                .empName(form.getEmpName())
                .email(form.getEmail())
                .phone(form.getPhone())
                .deptId(form.getDeptId())
                .jobCode(form.getJobCode())
                .salary(form.getSalary())
                .managerId(form.getManagerId())
                .hireDate(form.getHireDate())
                .active(form.isActive())
                .memo(form.getMemo())
                .build();
    }
}
