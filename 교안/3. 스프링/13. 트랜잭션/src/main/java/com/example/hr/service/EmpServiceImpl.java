package com.example.hr.service;

import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpForm;
import com.example.hr.dto.EmpSearchCond;
import com.example.hr.dto.PageResult;
import com.example.hr.mapper.EmpMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * Day 13 실습답안 — 문제 1(읽기/쓰기 배치), 문제 2(registerWithHistory 롤백),
 * 문제 3(registerAndExport 체크 예외 함정), 문제 4(bulkRegister/registerOne 내부 호출 한계)를
 * 하나의 클래스로 병합. 클래스 레벨 readOnly=true, 쓰기 메서드에만 @Transactional 재정의(본문 3절).
 */
@Slf4j
@Service
@Transactional(readOnly = true)      // 기본은 읽기 전용
@RequiredArgsConstructor
public class EmpServiceImpl implements EmpService {

    private final EmpMapper empMapper;

    @Override
    public PageResult<Emp> search(EmpSearchCond cond) {
        List<Emp> content = empMapper.findPage(cond);
        long total = empMapper.countPage(cond);
        return new PageResult<>(content, total, cond.getPage(), cond.getSize());
    }

    @Override
    @Transactional                    // 이 메서드만 읽기/쓰기
    public Long register(EmpForm form) {
        log.debug("register tx");
        Emp emp = toEmp(form);
        empMapper.insert(emp);
        return emp.getEmpId();
    }

    @Override
    @Transactional
    public void modify(Long id, EmpForm form) {
        empMapper.update(id, form);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        empMapper.deleteById(id);
    }

    // 문제 2. 롤백되는 등록 — 사원 저장 후 예외가 나면 INSERT EMP 도 함께 롤백된다.
    @Transactional
    public Long registerWithHistory(EmpForm form) {
        Emp emp = toEmp(form);
        empMapper.insert(emp);                       // 1) 사원 저장
        if (true) throw new IllegalStateException("이력 저장 실패");   // 2) 예외
        // historyMapper.insert(...);
        return emp.getEmpId();
    }

    // 문제 3. 체크 예외의 함정 — 기본 규칙은 언체크만 롤백이므로 IOException 은 커밋된다.
    @Transactional
    public void registerAndExport(EmpForm form) throws IOException {
        empMapper.insert(toEmp(form));
        throw new IOException("파일 실패");     // 체크 예외
    }

    // 문제 4. 내부 호출 한계 — this.registerOne(f) 는 프록시를 안 거쳐 @Transactional 이 무시된다.
    public void bulkRegister(List<EmpForm> forms) {
        for (EmpForm f : forms) this.registerOne(f);   // this. → 프록시 미경유
    }

    @Transactional
    public void registerOne(EmpForm f) {
        empMapper.insert(toEmp(f));
    }

    private Emp toEmp(EmpForm form) {
        // Day 9 EmpForm → Emp 변환(구체 구현은 Day 9 참고)
        throw new UnsupportedOperationException("Day 9 참고");
    }
}
