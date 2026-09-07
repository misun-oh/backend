package com.example.hr.repository;

import java.util.List;
import java.util.Optional;
import com.example.hr.domain.Dept;

public interface DeptRepository {
    List<Dept> findAll();
    Optional<Dept> findById(Long id);
    Dept save(Dept dept);
    void deleteById(Long id);
}
