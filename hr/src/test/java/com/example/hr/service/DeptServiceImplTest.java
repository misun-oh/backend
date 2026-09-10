package com.example.hr.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.hr.common.exception.DeptInUseException;
import com.example.hr.common.exception.NotFoundException;
import com.example.hr.domain.Dept;
import com.example.hr.dto.DeptForm;
import com.example.hr.mapper.DeptMapper;
import com.example.hr.mapper.EmpMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DeptServiceImplTest {

    @Mock
    DeptMapper deptMapper;

    @Mock
    EmpMapper empMapper;

    @InjectMocks
    DeptServiceImpl deptService;

    @Test
    void 없는_부서를_조회하면_NotFoundException() {
        given(deptMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> deptService.get(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 등록하면_매퍼에_insert하고_생성된_id를_반환한다() {
        willAnswer(invocation -> {
            Dept dept = invocation.getArgument(0);
            dept.setDeptId(10L);
            return null;
        }).given(deptMapper).insert(any(Dept.class));

        DeptForm form = new DeptForm();
        form.setDeptName("신사업개발부");
        form.setLocation("서울");

        Long deptId = deptService.register(form);

        assertThat(deptId).isEqualTo(10L);
        ArgumentCaptor<Dept> captor = ArgumentCaptor.forClass(Dept.class);
        then(deptMapper).should().insert(captor.capture());
        assertThat(captor.getValue().getDeptName()).isEqualTo("신사업개발부");
    }

    @Test
    void 없는_부서를_수정하면_NotFoundException이고_update는_호출되지_않는다() {
        given(deptMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> deptService.modify(999L, new DeptForm()))
                .isInstanceOf(NotFoundException.class);

        then(deptMapper).should(never()).update(any(Dept.class));
    }

    @Test
    void 소속_사원이_있으면_삭제가_막힌다() {
        given(deptMapper.selectById(5L)).willReturn(Dept.builder().deptId(5L).deptName("해외영업1부").build());
        given(empMapper.countByDeptId(5L)).willReturn(5);

        assertThatThrownBy(() -> deptService.remove(5L))
                .isInstanceOf(DeptInUseException.class)
                .hasMessageContaining("5");

        then(deptMapper).should(never()).deleteById(anyLong());
    }

    @Test
    void 소속_사원이_없으면_정상적으로_삭제된다() {
        given(deptMapper.selectById(3L)).willReturn(Dept.builder().deptId(3L).deptName("마케팅부").build());
        given(empMapper.countByDeptId(3L)).willReturn(0);

        deptService.remove(3L);

        then(deptMapper).should().deleteById(3L);
    }
}
