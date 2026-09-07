package com.example.hr.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;
import com.example.hr.domain.Emp;

/** Day 7 에서 MyBatis EmpMapper 로 교체될 임시 구현 */
@Repository
public class EmpMemoryRepository implements EmpRepository {

    private final Map<Long, Emp> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(200);

    @Override public List<Emp> findAll() { return new ArrayList<>(store.values()); }

    @Override public Optional<Emp> findById(Long id) { return Optional.ofNullable(store.get(id)); }

    @Override public Emp save(Emp emp) {
        Long id = (emp.getEmpId() != null) ? emp.getEmpId() : seq.incrementAndGet();
        Emp toStore = Emp.builder()
                .empId(id).empName(emp.getEmpName()).email(emp.getEmail())
                .deptId(emp.getDeptId()).salary(emp.getSalary())
                .hireDate(emp.getHireDate()).active(emp.isActive())
                .build();
        store.put(id, toStore);
        return toStore;
    }

    @Override public void deleteById(Long id) { store.remove(id); }
}
