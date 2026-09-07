package com.example.hr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class EmpServiceTest {

    @Autowired EmpService empService;

    @Test void 전체_21명() {
        assertThat(empService.findAll()).hasSize(21);
    }

    @Test void 사번200은_곽상혁() {
        assertThat(empService.get(200L).getEmpName()).isEqualTo("곽상혁");
    }

    @Test void 없는_사번은_예외() {
        assertThatThrownBy(() -> empService.get(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("사원 없음");
    }
}
