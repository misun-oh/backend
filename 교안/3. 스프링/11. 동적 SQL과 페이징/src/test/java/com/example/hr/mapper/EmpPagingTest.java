package com.example.hr.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.hr.dto.EmpDto;
import com.example.hr.dto.EmpSearchCond;
import com.example.hr.dto.EmpSort;
import com.example.hr.dto.PageDto;

/**
 * 2_페이징_실습답안.md — 문제 2, 3, 4.
 */
@SpringBootTest
class EmpPagingTest {

    @Autowired
    EmpMapper mapper;

    @Test
    void 첫페이지_10건_총건수21() {
        var c = new EmpSearchCond();
        assertThat(mapper.selectByCond(c)).hasSize(10);
        assertThat(mapper.countByCond(c)).isEqualTo(21);
    }

    @Test
    void 마지막페이지_1건_총건수는그대로() {
        var c = new EmpSearchCond();
        c.setPage(3);
        assertThat(mapper.selectByCond(c)).hasSize(1);
        assertThat(mapper.countByCond(c)).isEqualTo(21);
    }

    @Test
    void 첫페이지_이전버튼없음() {
        PageDto pageDto = new PageDto(1, 21);
        assertThat(pageDto.getSPageNo()).isEqualTo(1);
        assertThat(pageDto.isPrev()).isFalse();
    }

    @Test
    void 마지막페이지_다음버튼없음() {
        // size=10 고정이므로 21건이면 마지막 페이지는 3
        PageDto pageDto = new PageDto(3, 21);
        assertThat(pageDto.isNext()).isFalse();
    }

    @Test
    void 급여내림차순_첫행은_곽상혁() {
        var c = new EmpSearchCond();
        c.setSort(EmpSort.SALARY);
        var list = mapper.selectByCond(c);
        assertThat(list.get(0).getEmpName()).isEqualTo("곽상혁"); // 8,000,000
    }
}
