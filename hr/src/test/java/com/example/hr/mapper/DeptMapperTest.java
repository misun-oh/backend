package com.example.hr.mapper;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.hr.domain.Dept;
import com.example.hr.dto.DeptListRow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DeptMapper를 실제 MySQL에 붙여 검증한다. EmpMapperTest와 같은 이유로 @Tag("integration").
 * ./gradlew integrationTest 로 실행(MySQL 필요).
 */
@SpringBootTest
@Transactional
@Tag("integration")
class DeptMapperTest {

    @Autowired
    DeptMapper deptMapper;

    @Test
    void selectAll_은_초기데이터_9개_부서() {
        assertThat(deptMapper.selectAll()).hasSize(9);
    }

    @Test
    void selectAllWithStats_는_인원수와_평균급여를_집계한다() {
        List<DeptListRow> rows = deptMapper.selectAllWithStats();

        assertThat(rows).hasSize(9);

        DeptListRow 해외영업1부 = rows.stream()
                .filter(r -> r.getDeptId().equals(5L))
                .findFirst().orElseThrow();
        assertThat(해외영업1부.getMemberCount()).isEqualTo(5);
        assertThat(해외영업1부.getAvgSalary()).isEqualTo(2_752_000);

        DeptListRow 마케팅부 = rows.stream()
                .filter(r -> r.getDeptId().equals(3L))
                .findFirst().orElseThrow();
        assertThat(마케팅부.getMemberCount()).isZero();
        assertThat(마케팅부.getAvgSalary()).isZero();
    }

    @Test
    void selectById_없는_부서면_null() {
        assertThat(deptMapper.selectById(999L)).isNull();
    }

    @Test
    void insert_하면_생성된_키가_채워진다() {
        Dept dept = Dept.builder().deptName("신사업개발부").location("서울").build();

        deptMapper.insert(dept);

        assertThat(dept.getDeptId()).isNotNull();
        assertThat(deptMapper.selectById(dept.getDeptId()).getDeptName()).isEqualTo("신사업개발부");
    }

    @Test
    void update_하면_값이_바뀐다() {
        Dept dept = deptMapper.selectById(3L);
        dept.setLocation("부산");

        deptMapper.update(dept);

        assertThat(deptMapper.selectById(3L).getLocation()).isEqualTo("부산");
    }

    @Test
    void deleteById_하면_사라진다() {
        // 3번(마케팅부)은 소속 사원이 0명이라 삭제해도 FK 제약에 걸리지 않는다
        deptMapper.deleteById(3L);

        assertThat(deptMapper.selectById(3L)).isNull();
        assertThat(deptMapper.selectAll()).hasSize(8);
    }
}
