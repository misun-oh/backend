package com.example.hr.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeptFormTest {

    @Test
    void deptId가_없으면_새_등록_폼이다() {
        DeptForm form = new DeptForm();

        assertThat(form.isNew()).isTrue();
    }

    @Test
    void deptId가_있으면_수정_폼이다() {
        DeptForm form = new DeptForm();
        form.setDeptId(1L);

        assertThat(form.isNew()).isFalse();
    }
}
