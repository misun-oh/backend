package com.example.hr.service;

import com.example.hr.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Day 13 — 본문 1절. 계좌 이체 예제. 메서드 정상 반환 시 COMMIT, RuntimeException 시 자동 ROLLBACK.
 */
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountMapper accountMapper;

    @Transactional
    public void transfer(Long from, Long to, int amount) {
        accountMapper.minusBalance(from, amount);   // 출금
        accountMapper.plusBalance(to, amount);      // 입금
        // 메서드가 정상 반환되면 → COMMIT
        // 도중에 RuntimeException 이 나면 → 자동 ROLLBACK (출금도 취소됨)
    }
}
