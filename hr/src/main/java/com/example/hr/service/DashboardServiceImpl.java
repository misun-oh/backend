package com.example.hr.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hr.domain.Emp;
import com.example.hr.dto.DashboardStats;
import com.example.hr.dto.DeptListRow;
import com.example.hr.mapper.DeptMapper;
import com.example.hr.mapper.EmpMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_HIRE_COUNT = 5;

    private final EmpMapper empMapper;
    private final DeptMapper deptMapper;

    @Override
    public DashboardStats getStats() {
        long totalEmp = empMapper.countAll();
        long activeEmp = empMapper.countActive();
        long avgSalary = empMapper.avgSalary();
        long maxSalary = empMapper.maxSalary();
        long minSalary = empMapper.minSalary();

        List<DeptListRow> deptStats = deptMapper.selectAllWithStats();
        long deptCount = deptStats.size();
        long emptyDeptCount = deptStats.stream().filter(d -> d.getMemberCount() == 0).count();

        List<DeptListRow> deptBars = deptStats.stream()
                .filter(d -> d.getMemberCount() > 0)
                .sorted(Comparator.comparingInt(DeptListRow::getMemberCount).reversed())
                .collect(Collectors.toList());

        List<Emp> recentHires = empMapper.selectRecentHires(RECENT_HIRE_COUNT);

        return new DashboardStats(totalEmp, activeEmp, deptCount, emptyDeptCount,
                avgSalary, maxSalary, minSalary, recentHires, deptBars);
    }
}
