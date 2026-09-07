package com.example.hr.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import com.example.hr.domain.Emp;
import com.example.hr.mapper.EmpMapper;

/** 방법 A: EmpRepository 인터페이스를 유지한 채 구현만 MyBatis로 교체 (insert/update/delete는 Day 9) */
@Repository
@RequiredArgsConstructor
public class EmpRepositoryMybatis implements EmpRepository {
    private final EmpMapper empMapper;

    @Override public List<Emp> findAll() { return empMapper.findAll(); }

    @Override public Optional<Emp> findById(Long id) { return Optional.ofNullable(empMapper.findById(id)); }

    @Override public Emp save(Emp emp) {
        /* insert/update — Day 9 */
        return emp;
    }

    @Override public void deleteById(Long id) {
        /* Day 9 */
    }
}
