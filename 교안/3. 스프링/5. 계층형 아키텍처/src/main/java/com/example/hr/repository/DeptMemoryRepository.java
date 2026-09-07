package com.example.hr.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;
import com.example.hr.domain.Dept;

@Repository
public class DeptMemoryRepository implements DeptRepository {
    private final Map<Long, Dept> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(10);

    @Override public List<Dept> findAll() { return new ArrayList<>(store.values()); }

    @Override public Optional<Dept> findById(Long id) { return Optional.ofNullable(store.get(id)); }

    @Override public Dept save(Dept d) {
        Long id = d.getDeptId() != null ? d.getDeptId() : seq.incrementAndGet();
        Dept toStore = Dept.builder().deptId(id).deptName(d.getDeptName()).location(d.getLocation()).build();
        store.put(id, toStore);
        return toStore;
    }

    @Override public void deleteById(Long id) { store.remove(id); }
}
