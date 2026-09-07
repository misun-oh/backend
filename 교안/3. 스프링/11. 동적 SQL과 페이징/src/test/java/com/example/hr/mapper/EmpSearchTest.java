package com.example.hr.mapper;

import com.example.hr.dto.EmpSearchCond;
import com.example.hr.dto.EmpSort;
import com.example.hr.domain.Emp;
import com.example.hr.service.EmpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Day 11 실습답안 — 문제 2~5의 테스트를 하나의 클래스로 병합.
 * 문제 2/5 는 원문에 클래스로 감싸져 있고(@SpringBootTest @Transactional),
 * 문제 3/4 의 테스트 메서드들은 감싸는 클래스 없이 이어지는 조각이라 같은 클래스에 합쳤다.
 */
@SpringBootTest
@Transactional
class EmpSearchTest {

    @Autowired
    EmpMapper mapper;

    @Autowired
    EmpService empService;

    private EmpSearchCond cond() {
        return new EmpSearchCond();
    }

    @Test
    void 조건없음_21() {
        assertThat(mapper.countPage(cond())).isEqualTo(21);
    }

    @Test
    void 재직만_20() {
        var c = cond();
        c.setActiveOnly(true);
        assertThat(mapper.countPage(c)).isEqualTo(20);
    }

    @Test
    void D5_5명() {
        var c = cond();
        c.setDeptId("D5");
        assertThat(mapper.countPage(c)).isEqualTo(5);
    }

    @Test
    void D5_재직_5명() {
        var c = cond();
        c.setDeptId("D5");
        c.setActiveOnly(true);
        assertThat(mapper.countPage(c)).isEqualTo(5);
    }

    @Test
    void 키워드_김() {
        var c = cond();
        c.setKeyword("김");
        assertThat(mapper.findPage(c)).extracting("empName").allMatch(n -> ((String) n).contains("김"));
    }

    // 문제 3. 페이징
    @Test
    void 첫페이지_10건_총3페이지() {
        var c = new EmpSearchCond();
        var p = empService.search(c);
        assertThat(p.content()).hasSize(10);
        assertThat(p.totalElements()).isEqualTo(21);
        assertThat(p.totalPages()).isEqualTo(3);
        assertThat(p.hasNext()).isTrue();
    }

    @Test
    void 마지막페이지_1건() {
        var c = new EmpSearchCond();
        c.setPage(3);
        var p = empService.search(c);
        assertThat(p.content()).hasSize(1);
        assertThat(p.hasNext()).isFalse();
    }

    // 문제 4. 정렬
    @Test
    void 급여내림차순_첫행은_곽상혁() {
        var c = new EmpSearchCond();
        c.setSort(EmpSort.SALARY);
        var p = empService.search(c);
        assertThat(p.content().get(0).getEmpName()).isEqualTo("곽상혁");   // 8,000,000
    }

    // 문제 5. <foreach> IN
    @Test
    void 세개중_존재하는_두건() {
        assertThat(mapper.findByIds(List.of(200L, 205L, 999L)))
                .extracting(Emp::getEmpId).containsExactlyInAnyOrder(200L, 205L);
    }
}
