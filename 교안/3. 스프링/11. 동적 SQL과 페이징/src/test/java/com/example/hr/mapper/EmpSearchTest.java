package com.example.hr.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.hr.dto.EmpDto;
import com.example.hr.dto.EmpSearchCond;

/**
 * 1_동적SQL과검색_실습답안.md — 문제 2, 3.
 * 아직 LIMIT 이 없으므로 selectByCond(cond).size() 가 곧 조건에 맞는 전체 건수.
 */
@SpringBootTest
class EmpSearchTest {

    @Autowired
    EmpMapper mapper;

    private EmpSearchCond cond() {
        return new EmpSearchCond();
    }

    @Test
    void 조건없음_21() {
        assertThat(mapper.selectByCond(cond())).hasSize(21);
    }

    @Test
    void 재직만_20() {
        var c = cond();
        c.setWorkingOnly(true);
        assertThat(mapper.selectByCond(c)).hasSize(20);
    }

    @Test
    void D5_5명() {
        var c = cond();
        c.setDeptId("D5");
        assertThat(mapper.selectByCond(c)).hasSize(5);
    }

    @Test
    void D5_재직_5명() {
        var c = cond();
        c.setDeptId("D5");
        c.setWorkingOnly(true);
        assertThat(mapper.selectByCond(c)).hasSize(5);
    }

    @Test
    void 키워드_김() {
        var c = cond();
        c.setKeyword("김");
        assertThat(mapper.selectByCond(c)).extracting("empName").allMatch(n -> ((String) n).contains("김"));
    }

    // 문제 3. <foreach> IN
    @Test
    void 세개중_존재하는_두건() {
        assertThat(mapper.findByIds(List.of(200, 205, 999)))
                .extracting(EmpDto::getEmpId).containsExactlyInAnyOrder(200, 205);
    }
}
