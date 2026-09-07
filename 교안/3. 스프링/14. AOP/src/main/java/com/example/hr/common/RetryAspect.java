package com.example.hr.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Day 14 심화 4절 — @Retryable 이 붙은 메서드를 지정한 예외에 대해 재시도.
 * 운영에서는 Resilience4j @Retry/@CircuitBreaker 를 쓰는 편(백오프·지터·메트릭 포함).
 */
@Aspect
@Component
@Slf4j
public class RetryAspect {

    @Around("@annotation(r)")
    public Object retry(ProceedingJoinPoint pjp, Retryable r) throws Throwable {
        int attempt = 0;
        while (true) {
            try {
                return pjp.proceed();
            } catch (Throwable e) {
                if (!r.on().isInstance(e) || ++attempt >= r.max()) throw e;
                log.warn("{} 재시도 {}/{} : {}", pjp.getSignature().toShortString(), attempt, r.max(), e.toString());
                Thread.sleep(200L * attempt);
            }
        }
    }
}
