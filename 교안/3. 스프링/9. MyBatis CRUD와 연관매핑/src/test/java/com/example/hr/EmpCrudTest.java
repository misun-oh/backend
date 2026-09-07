package com.example.hr;

import com.example.hr.domain.Emp;
import com.example.hr.mapper.EmpMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class EmpCrudTest {

    @Autowired EmpMapper empMapper;

    @Test
    void 등록_수정_삭제_흐름() {
        // 등록
        Emp e = Emp.builder().empId(900L).empName("실험").email("x@ex.com")
                .deptId(5L).jobCode("J7").salary(2_000_000).hireDate(LocalDate.now()).build();
        assertThat(empMapper.insert(e)).isEqualTo(1);
        assertThat(empMapper.findById(900L).getEmpName()).isEqualTo("실험");

        // 수정
        e = Emp.builder().empId(900L).empName("실험2").email("x@ex.com")
                .deptId(6L).salary(2_100_000).hireDate(LocalDate.now()).build();
        assertThat(empMapper.update(e)).isEqualTo(1);
        assertThat(empMapper.findById(900L).getEmpName()).isEqualTo("실험2");

        // 삭제
        assertThat(empMapper.deleteById(900L)).isEqualTo(1);
        assertThat(empMapper.findById(900L)).isNull();
    }   // @Transactional → 롤백. DB는 21명 그대로

    @Test
    void 목록에_부서명이_채워진다() {
        List<Emp> rows = empMapper.findList();
        assertThat(rows).hasSize(21);
        assertThat(rows).anyMatch(e -> "총무부".equals(e.getDeptName()));   // 곽상혁의 부서
        assertThat(rows).allSatisfy(e -> assertThat(e.getEmpName()).isNotBlank());
    }

    @Test
    void 상세에_부서객체() {
        Emp e = empMapper.findByIdWithDept(205L);
        assertThat(e.getDept().getDeptName()).isEqualTo("해외영업1부");
    }
}
