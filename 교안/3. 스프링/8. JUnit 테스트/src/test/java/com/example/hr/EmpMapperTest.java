package com.example.hr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional                    // ★ 각 테스트 끝나면 자동 롤백
class EmpMapperTest {

    @Autowired EmpMapper empMapper;

    @Test
    void findAll_로_21명을_가져온다() {
        List<Emp> list = empMapper.findAll();
        assertThat(list).hasSize(21);
        assertThat(list).extracting(Emp::getEmpName).contains("곽상혁", "박지민");
    }

    @Test
    void findById_없는_사번이면_null() {
        assertThat(empMapper.findById(999L)).isNull();
    }

    @Test
    void insert_하면_한_명_늘어난다() {
        int before = empMapper.findAll().size();

        Emp emp = Emp.builder()
                .empName("테스트사원").email("t@ex.com").deptId(5L)
                .salary(2500000).hireDate(LocalDate.now()).active(true)
                .build();
        empMapper.insert(emp);            // Day 9에서 만들 insert

        assertThat(empMapper.findAll()).hasSize(before + 1);
        assertThat(emp.getEmpId()).isNotNull();   // 생성 키가 채워졌는지
    }
    // 이 테스트가 끝나면 @Transactional 이 롤백 → DB의 EMP 는 다시 21명
}
