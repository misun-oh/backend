package com.example.hr.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hr.common.exception.DeptInUseException;
import com.example.hr.common.exception.NotFoundException;
import com.example.hr.domain.Dept;
import com.example.hr.dto.DeptForm;
import com.example.hr.dto.DeptListRow;
import com.example.hr.mapper.DeptMapper;
import com.example.hr.mapper.EmpMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;
    private final EmpMapper empMapper;

    @Override
    public List<DeptListRow> listWithStats() {
        return deptMapper.selectAllWithStats();
    }

    @Override
    public List<Dept> listAll() {
        return deptMapper.selectAll();
    }

    @Override
    public Dept get(Long deptId) {
        Dept dept = deptMapper.selectById(deptId);
        if (dept == null) {
            throw new NotFoundException("부서를 찾을 수 없습니다. (deptId=" + deptId + ")");
        }
        return dept;
    }

    @Override
    @Transactional
    public Long register(DeptForm form) {
        Dept dept = Dept.builder()
                .deptName(form.getDeptName())
                .location(form.getLocation())
                .build();
        deptMapper.insert(dept);
        return dept.getDeptId();
    }

    @Override
    @Transactional
    public void modify(Long deptId, DeptForm form) {
        get(deptId); // 존재 확인
        Dept dept = Dept.builder()
                .deptId(deptId)
                .deptName(form.getDeptName())
                .location(form.getLocation())
                .build();
        deptMapper.update(dept);
    }

    @Override
    @Transactional
    public void remove(Long deptId) {
        get(deptId); // 존재 확인
        int memberCount = empMapper.countByDeptId(deptId);
        if (memberCount > 0) {
            throw new DeptInUseException("소속 사원이 " + memberCount + "명 있어 부서를 삭제할 수 없습니다.");
        }
        deptMapper.deleteById(deptId);
    }
}
