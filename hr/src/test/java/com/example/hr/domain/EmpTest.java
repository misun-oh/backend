package com.example.hr.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmpTest {

    @Test
    void 부서가_없으면_미배정으로_표시한다() {
        Emp emp = Emp.builder().empName("한규원").deptName(null).jobName("사원").build();

        assertThat(emp.getDeptNameOrUnassigned()).isEqualTo("미배정");
    }

    @Test
    void 부서가_있으면_부서명을_그대로_표시한다() {
        Emp emp = Emp.builder().empName("박지민").deptName("해외영업1부").jobName("부장").build();

        assertThat(emp.getDeptNameOrUnassigned()).isEqualTo("해외영업1부");
    }

    @Test
    void 관리자_선택옵션_라벨은_이름과_부서_직급을_함께_보여준다() {
        Emp emp = Emp.builder().empName("박지민").deptName("해외영업1부").jobName("부장").build();

        assertThat(emp.getManagerOptionLabel()).isEqualTo("박지민 (해외영업1부 부장)");
    }

    @Test
    void 부서가_없는_관리자_후보는_미배정으로_표시한다() {
        Emp emp = Emp.builder().empName("한규원").deptName(null).jobName("사원").build();

        assertThat(emp.getManagerOptionLabel()).isEqualTo("한규원 (미배정 사원)");
    }
}
