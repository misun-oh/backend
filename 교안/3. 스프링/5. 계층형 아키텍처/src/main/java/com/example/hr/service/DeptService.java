package com.example.hr.service;

import java.util.List;
import com.example.hr.domain.Dept;
import com.example.hr.dto.DeptForm;

public interface DeptService {
    List<Dept> findAll();
    Long register(DeptForm form);
    void remove(Long id);
}
