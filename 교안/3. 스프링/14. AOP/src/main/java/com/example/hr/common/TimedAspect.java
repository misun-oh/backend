package com.example.hr.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Day 14 실습답안 문제 3 — @Timed 가 붙은 메서드만 시간을 재고, warnMs 를 넘으면 warn 로그.
 */
@Slf4j
@Aspect
@Component
public class TimedAspect {

    @Around("@annotation(timed)")
    public Object measure(ProceedingJoinPoint pjp, Timed timed) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            String sig = pjp.getSignature().toShortString();
            if (ms >= timed.warnMs()) log.warn("[느림] {} {}ms (기준 {}ms)", sig, ms, timed.warnMs());
            else log.debug("[시간] {} {}ms", sig, ms);
        }
    }
}
