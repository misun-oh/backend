package com.example.hr.dto;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardStatsTest {

    @Test
    void 퇴사자수는_전체에서_재직자를_뺀_값이다() {
        DashboardStats stats = new DashboardStats(21, 20, 9, 2, 3180000, 8000000, 1380000,
                List.of(), List.of());

        assertThat(stats.getResignedEmp()).isEqualTo(1);
    }

    @Test
    void 운영중인_부서수는_전체에서_미배정_부서를_뺀_값이다() {
        DashboardStats stats = new DashboardStats(21, 20, 9, 2, 3180000, 8000000, 1380000,
                List.of(), List.of());

        assertThat(stats.getOperatingDeptCount()).isEqualTo(7);
    }

    @Test
    void 부서별_인원_중_최대값을_구한다() {
        List<DeptListRow> deptStats = List.of(
                new DeptListRow(5L, "해외영업1부", "일본", 5, 2752000),
                new DeptListRow(9L, "총무부", "서울", 3, 5900000));

        DashboardStats stats = new DashboardStats(21, 20, 9, 2, 3180000, 8000000, 1380000,
                List.of(), deptStats);

        assertThat(stats.getMaxDeptMemberCount()).isEqualTo(5);
    }

    @Test
    void 부서_통계가_없으면_최대_인원은_0이다() {
        DashboardStats stats = new DashboardStats(0, 0, 0, 0, 0, 0, 0, List.of(), List.of());

        assertThat(stats.getMaxDeptMemberCount()).isZero();
    }
}
