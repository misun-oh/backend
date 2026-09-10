package com.example.hr.dto;

import java.util.List;

import com.example.hr.domain.Emp;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 대시보드 화면 전용 집계 DTO. */
@Getter
@AllArgsConstructor
public class DashboardStats {

    private final long totalEmp;
    private final long activeEmp;
    private final long deptCount;
    private final long emptyDeptCount;
    private final long avgSalary;
    private final long maxSalary;
    private final long minSalary;
    private final List<Emp> recentHires;
    private final List<DeptListRow> deptMemberStats;

    public long getResignedEmp() {
        return totalEmp - activeEmp;
    }

    public long getOperatingDeptCount() {
        return deptCount - emptyDeptCount;
    }

    public int getMaxDeptMemberCount() {
        return deptMemberStats.stream().mapToInt(DeptListRow::getMemberCount).max().orElse(0);
    }
}
