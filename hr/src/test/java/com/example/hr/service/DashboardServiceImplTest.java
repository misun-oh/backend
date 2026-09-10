package com.example.hr.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.hr.domain.Emp;
import com.example.hr.dto.DashboardStats;
import com.example.hr.dto.DeptListRow;
import com.example.hr.mapper.DeptMapper;
import com.example.hr.mapper.EmpMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    EmpMapper empMapper;

    @Mock
    DeptMapper deptMapper;

    @InjectMocks
    DashboardServiceImpl dashboardService;

    @Test
    void 집계값과_부서별_인원을_많은_순으로_정리한다() {
        given(empMapper.countAll()).willReturn(21L);
        given(empMapper.countActive()).willReturn(20L);
        given(empMapper.avgSalary()).willReturn(3180000L);
        given(empMapper.maxSalary()).willReturn(8000000L);
        given(empMapper.minSalary()).willReturn(1380000L);
        given(empMapper.selectRecentHires(5)).willReturn(List.of(
                Emp.builder().empId(215L).empName("한재헌").hireDate(LocalDate.of(2024, 4, 4)).build()));

        given(deptMapper.selectAllWithStats()).willReturn(List.of(
                new DeptListRow(1L, "인사관리부", "서울", 3, 2606000),
                new DeptListRow(3L, "마케팅부", "서울", 0, 0),
                new DeptListRow(5L, "해외영업1부", "일본", 5, 2752000),
                new DeptListRow(9L, "총무부", "서울", 3, 5900000)));

        DashboardStats stats = dashboardService.getStats();

        assertThat(stats.getTotalEmp()).isEqualTo(21);
        assertThat(stats.getActiveEmp()).isEqualTo(20);
        assertThat(stats.getResignedEmp()).isEqualTo(1);
        assertThat(stats.getDeptCount()).isEqualTo(4);
        assertThat(stats.getEmptyDeptCount()).isEqualTo(1);
        assertThat(stats.getOperatingDeptCount()).isEqualTo(3);
        assertThat(stats.getRecentHires()).extracting(Emp::getEmpName).containsExactly("한재헌");

        // 인원이 0명인 부서는 제외하고, 인원 많은 순으로 정렬된다
        assertThat(stats.getDeptMemberStats())
                .extracting(DeptListRow::getDeptName)
                .containsExactly("해외영업1부", "인사관리부", "총무부");
        assertThat(stats.getMaxDeptMemberCount()).isEqualTo(5);
    }
}
