package com.example.hr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class EmpMapperReadTest {
    @Autowired EmpMapper empMapper;

    @Test
    void findAll은_21명을_반환하고_첫_사원은_곽상혁이다() {
        List<Emp> list = empMapper.findAll();
        assertThat(list).hasSize(21);
        assertThat(list.get(0).getEmpName()).isEqualTo("곽상혁");
    }
}
