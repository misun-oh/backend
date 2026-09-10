package com.example.hr.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmpFormTest {

    @Test
    void empId가_없으면_새_등록_폼이다() {
        EmpForm form = new EmpForm();

        assertThat(form.isNew()).isTrue();
    }

    @Test
    void empId가_있으면_수정_폼이다() {
        EmpForm form = new EmpForm();
        form.setEmpId(205L);

        assertThat(form.isNew()).isFalse();
    }

    @Test
    void active_기본값은_true다() {
        EmpForm form = new EmpForm();

        assertThat(form.isActive()).isTrue();
    }
}
