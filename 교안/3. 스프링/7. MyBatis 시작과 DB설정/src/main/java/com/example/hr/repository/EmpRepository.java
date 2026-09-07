package com.example.hr.repository;

import java.util.List;
import java.util.Optional;
import com.example.hr.domain.Emp;

/** Day 5 에서 정의한 인터페이스. Day 7 부터 구현체가 메모리 → MyBatis(EmpRepositoryMybatis)로 바뀐다. */
public interface EmpRepository {
    List<Emp> findAll();
    Optional<Emp> findById(Long id);
    Emp save(Emp emp);
    void deleteById(Long id);
}
