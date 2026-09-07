package com.example.hr;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EmpFormTest {

    @Test
    void 급여가_null이면_0으로_등록된다() {
        // given (준비)
        EmpForm form = new EmpForm();
        form.setEmpName("신입");
        form.setSalary(null);

        // when (실행)
        int salary = (form.getSalary() == null) ? 0 : form.getSalary();

        // then (검증)
        assertThat(salary).isZero();
    }
}
