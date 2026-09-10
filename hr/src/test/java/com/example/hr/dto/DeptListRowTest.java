package com.example.hr.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeptListRowTest {

    @Test
    void 소속_인원이_없으면_평균급여는_대시로_표시한다() {
        DeptListRow row = new DeptListRow(3L, "마케팅부", "서울", 0, 0L);

        assertThat(row.getAvgSalaryDisplay()).isEqualTo("-");
    }

    @Test
    void 소속_인원이_있으면_평균급여를_천단위_콤마로_표시한다() {
        DeptListRow row = new DeptListRow(9L, "총무부", "서울", 3, 5_900_000L);

        assertThat(row.getAvgSalaryDisplay()).isEqualTo("5,900,000원");
    }
}
