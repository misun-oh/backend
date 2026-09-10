package com.example.hr.mapper;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpSearchCond;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EmpMapper를 실제 MySQL에 붙여 SQL을 검증한다.
 * 서비스 단위 테스트(EmpServiceImplTest)와 달리 여기서는 EmpMapper를 진짜로 쓴다 —
 * "매퍼가 하는 일을 흉내"가 아니라 "SQL 자체가 맞는지"를 확인하는 게 목적이다.
 *
 * 실행 전 준비: hr 데이터베이스가 떠 있어야 한다(README 참고).
 *   ./gradlew integrationTest 로 실행(기본 test에서는 제외됨).
 *
 * @Transactional -> 각 테스트가 끝나면 자동 롤백되어 data.sql로 채운 21명이 그대로 유지된다.
 */
@SpringBootTest
@Transactional
@Tag("integration")
class EmpMapperTest {

    @Autowired
    EmpMapper empMapper;

    @Test
    void countAll_은_초기데이터_21명() {
        assertThat(empMapper.countAll()).isEqualTo(21);
    }

    @Test
    void countActive_는_재직자_20명() {
        assertThat(empMapper.countActive()).isEqualTo(20);
    }

    @Test
    void selectById_없는_사번이면_null() {
        assertThat(empMapper.selectById(999L)).isNull();
    }

    @Test
    void selectById_있으면_부서명_직급명까지_조인해서_채운다() {
        Emp emp = empMapper.selectById(205L);

        assertThat(emp).isNotNull();
        assertThat(emp.getEmpName()).isEqualTo("박지민");
        assertThat(emp.getDeptName()).isEqualTo("해외영업1부");
        assertThat(emp.getJobName()).isEqualTo("부장");
    }

    @Test
    void countByDeptId_해외영업1부_5명() {
        assertThat(empMapper.countByDeptId(5L)).isEqualTo(5);
    }

    @Test
    void insert_하면_한_명_늘고_생성된_키가_채워진다() {
        long before = empMapper.countAll();

        Emp emp = Emp.builder()
                .empName("테스트사원").email("mapper-test@company.com")
                .deptId(5L).jobCode("J7").salary(2500000)
                .hireDate(LocalDate.now()).active(true).build();

        empMapper.insert(emp);

        assertThat(emp.getEmpId()).isNotNull();
        assertThat(empMapper.countAll()).isEqualTo(before + 1);
        // 이 테스트가 끝나면 @Transactional이 롤백 -> countAll()은 다시 21로 돌아간다
    }

    @Test
    void update_하면_값이_바뀐다() {
        Emp emp = empMapper.selectById(205L);
        emp.setSalary(4000000);

        empMapper.update(emp);

        assertThat(empMapper.selectById(205L).getSalary()).isEqualTo(4000000);
    }

    @Test
    void deleteById_하면_사라진다() {
        empMapper.deleteById(220L);

        assertThat(empMapper.selectById(220L)).isNull();
        assertThat(empMapper.countAll()).isEqualTo(20);
    }

    @Test
    void selectByCond_재직자만_필터링() {
        EmpSearchCond cond = new EmpSearchCond();
        cond.setWorkingOnly(true);

        List<Emp> list = empMapper.selectByCond(cond);

        assertThat(list).allMatch(Emp::isActive);
        assertThat(empMapper.countByCond(cond)).isEqualTo(20);
    }

    @Test
    void selectByCond_이름으로_검색() {
        EmpSearchCond cond = new EmpSearchCond();
        cond.setKeyword("박지민");
        cond.setWorkingOnly(false);

        List<Emp> list = empMapper.selectByCond(cond);

        assertThat(list).extracting(Emp::getEmpName).containsExactly("박지민");
    }
}
