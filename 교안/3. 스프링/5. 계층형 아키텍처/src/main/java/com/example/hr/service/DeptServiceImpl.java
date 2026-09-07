package com.example.hr.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.example.hr.domain.Dept;
import com.example.hr.dto.DeptForm;
import com.example.hr.repository.DeptRepository;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {
    private final DeptRepository deptRepository;

    @Override
    public List<Dept> findAll() { return deptRepository.findAll(); }

    @Override
    public Long register(DeptForm form) {
        Dept saved = deptRepository.save(Dept.builder()
                .deptName(form.getDeptName()).location(form.getLocation()).build());
        return saved.getDeptId();
    }

    @Override
    public void remove(Long id) {
        deptRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("부서 없음: " + id));
        deptRepository.deleteById(id);
    }
}
