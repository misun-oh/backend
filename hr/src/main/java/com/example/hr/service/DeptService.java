package com.example.hr.service;

import java.util.List;

import com.example.hr.domain.Dept;
import com.example.hr.dto.DeptForm;
import com.example.hr.dto.DeptListRow;

public interface DeptService {

    List<DeptListRow> listWithStats();

    List<Dept> listAll();

    Dept get(Long deptId);

    Long register(DeptForm form);

    void modify(Long deptId, DeptForm form);

    /** 소속 사원이 있으면 DeptInUseException */
    void remove(Long deptId);
}
