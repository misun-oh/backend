package com.example.hr.service;

import java.util.List;
import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpForm;

public interface EmpService {
    List<Emp> findAll();
    Emp get(Long id);
    Long register(EmpForm form);
    void modify(Long id, EmpForm form);
    void remove(Long id);
}
