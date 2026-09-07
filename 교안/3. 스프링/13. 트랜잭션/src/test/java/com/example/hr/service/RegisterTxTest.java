package com.example.hr.service;

import com.example.hr.mapper.EmpMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Day 13 실습답안 — 문제 2. registerWithHistory 롤백 검증(테스트에 @Transactional 없음,
 * 대신 @AfterEach 로 직접 정리).
 */
@SpringBootTest
class RegisterTxTest {

    @Autowired
    EmpService empService;

    @Autowired
    EmpMapper empMapper;

    Long createdId;

    @AfterEach
    void clean() {
        if (createdId != null) empMapper.deleteById(createdId);
    }

    @Test
    void 트랜잭션이면_롤백() {
        int before = empMapper.findAll().size();
        assertThatThrownBy(() -> empService.registerWithHistory(form(901L)))
                .isInstanceOf(RuntimeException.class);
        assertThat(empMapper.findAll()).hasSize(before);   // 롤백 → 그대로
    }
}
