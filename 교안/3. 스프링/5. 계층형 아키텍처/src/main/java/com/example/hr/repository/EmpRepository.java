package com.example.hr.repository;

import java.util.List;
import java.util.Optional;
import com.example.hr.domain.Emp;

public interface EmpRepository {
    List<Emp> findAll();
    Optional<Emp> findById(Long id);
    Emp save(Emp emp);
    void deleteById(Long id);
}
