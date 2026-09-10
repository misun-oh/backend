package com.example.hr.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.hr.common.exception.NotFoundException;
import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpForm;
import com.example.hr.dto.EmpSearchCond;
import com.example.hr.dto.PageResult;
import com.example.hr.mapper.EmpMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;

@ExtendWith(MockitoExtension.class)
class EmpServiceImplTest {

    @Mock
    EmpMapper empMapper;

    @InjectMocks
    EmpServiceImpl empService;

    @Test
    void 없는_사원을_조회하면_NotFoundException() {
        given(empMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> empService.get(999L))
                .isInstanceOf(NotFoundException.class);

        then(empMapper).should().selectById(999L);
    }

    @Test
    void 있는_사원을_조회하면_그대로_반환한다() {
        Emp emp = Emp.builder().empId(205L).empName("박지민").build();
        given(empMapper.selectById(205L)).willReturn(emp);

        Emp result = empService.get(205L);

        assertThat(result).isSameAs(emp);
    }

    @Test
    void 등록하면_매퍼에_insert하고_생성된_id를_반환한다() {
        willAnswer(invocation -> {
            Emp emp = invocation.getArgument(0);
            emp.setEmpId(221L);
            return null;
        }).given(empMapper).insert(any(Emp.class));

        EmpForm form = new EmpForm();
        form.setEmpName("신입사원");
        form.setEmail("new@company.com");
        form.setDeptId(5L);
        form.setJobCode("J7");
        form.setSalary(2500000);
        form.setHireDate(LocalDate.of(2026, 9, 1));
        form.setActive(true);

        Long empId = empService.register(form);

        assertThat(empId).isEqualTo(221L);

        ArgumentCaptor<Emp> captor = ArgumentCaptor.forClass(Emp.class);
        then(empMapper).should().insert(captor.capture());
        assertThat(captor.getValue().getEmpName()).isEqualTo("신입사원");
        assertThat(captor.getValue().getDeptId()).isEqualTo(5L);
    }

    @Test
    void 없는_사원을_수정하면_NotFoundException이고_update는_호출되지_않는다() {
        given(empMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> empService.modify(999L, new EmpForm()))
                .isInstanceOf(NotFoundException.class);

        then(empMapper).should(org.mockito.Mockito.never()).update(any(Emp.class));
    }

    @Test
    void 있는_사원을_수정하면_매퍼_update가_empId와_함께_호출된다() {
        given(empMapper.selectById(205L)).willReturn(Emp.builder().empId(205L).build());

        EmpForm form = new EmpForm();
        form.setEmpName("박지민(수정)");
        form.setEmail("park_jm@company.com");
        form.setDeptId(5L);
        form.setJobCode("J3");
        form.setSalary(3600000);
        form.setHireDate(LocalDate.of(2015, 5, 20));
        form.setActive(true);

        empService.modify(205L, form);

        ArgumentCaptor<Emp> captor = ArgumentCaptor.forClass(Emp.class);
        then(empMapper).should().update(captor.capture());
        assertThat(captor.getValue().getEmpId()).isEqualTo(205L);
        assertThat(captor.getValue().getEmpName()).isEqualTo("박지민(수정)");
        assertThat(captor.getValue().getSalary()).isEqualTo(3600000);
    }

    @Test
    void 없는_사원을_삭제하면_NotFoundException이고_deleteById는_호출되지_않는다() {
        given(empMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> empService.remove(999L))
                .isInstanceOf(NotFoundException.class);

        then(empMapper).should(org.mockito.Mockito.never()).deleteById(anyLong());
    }

    @Test
    void 있는_사원을_삭제하면_매퍼_deleteById가_호출된다() {
        given(empMapper.selectById(205L)).willReturn(Emp.builder().empId(205L).build());

        empService.remove(205L);

        then(empMapper).should().deleteById(205L);
    }

    @Test
    void 검색하면_목록과_전체건수로_PageResult를_만든다() {
        EmpSearchCond cond = new EmpSearchCond();
        cond.setPage(2);
        List<Emp> content = List.of(Emp.builder().empId(1L).build(), Emp.builder().empId(2L).build());
        given(empMapper.selectByCond(cond)).willReturn(content);
        given(empMapper.countByCond(cond)).willReturn(25L);

        PageResult<Emp> result = empService.search(cond);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }
}
