package com.example.hr.service;

import com.example.hr.dto.EmpForm;
import com.example.hr.mapper.EmpMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Day 13 — 본문 7절. 롤백을 테스트로 검증.
 * 이 테스트 클래스에는 @Transactional 을 안 붙인다 — 테스트가 트랜잭션을 감싸면
 * registerWithHistory 내부 트랜잭션이 그 안에 참여해 버려 "서비스의 롤백"을 제대로 못 본다.
 */
@SpringBootTest
class EmpRegisterTxTest {

    @Autowired
    EmpService empService;

    @Autowired
    EmpMapper empMapper;

    @Test
    void 등록_도중_예외나면_아무것도_저장되지_않는다() {
        int before = empMapper.findAll().size();

        assertThatThrownBy(() ->
                empService.registerWithHistory(brokenForm())   // 중간에 RuntimeException
        ).isInstanceOf(RuntimeException.class);

        assertThat(empMapper.findAll()).hasSize(before);    // INSERT EMP 도 롤백됨
    }
}
