package com.example.hr.service;

import java.util.List;

import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpForm;
import com.example.hr.dto.EmpSearchCond;
import com.example.hr.dto.PageResult;

public interface EmpService {

    PageResult<Emp> search(EmpSearchCond cond);

    Emp get(Long empId);

    Long register(EmpForm form);

    void modify(Long empId, EmpForm form);

    void remove(Long empId);

    List<Emp> listActiveForDropdown();

    List<Emp> listSubordinates(Long managerId);

    long countAll();

    long countActive();
}
