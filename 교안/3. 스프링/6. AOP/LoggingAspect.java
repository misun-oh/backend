package com.example.hr.common;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Day 6 — 서비스/컨트롤러 메서드의 실행 시간·인자·반환값을 자동 로깅하는 애스펙트.
 * build.gradle 에 spring-boot-starter-aop 필요.
 * @Aspect + @Component 둘 다 있어야 동작한다.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.example.hr.service..*(..))")
    public void serviceLayer() {}

    @Pointcut("execution(* com.example.hr.controller..*(..))")
    public void controllerLayer() {}

    @Around("serviceLayer() || controllerLayer()")
    public Object logAndTime(ProceedingJoinPoint pjp) throws Throwable {
        String sig = pjp.getSignature().toShortString();
        long start = System.nanoTime();
        if (log.isDebugEnabled()) {
            log.debug("▶ {} args={}", sig, Arrays.toString(pjp.getArgs()));
        }
        try {
            Object result = pjp.proceed();               // 진짜 메서드 호출
            long ms = (System.nanoTime() - start) / 1_000_000;
            log.debug("◀ {} {}ms → {}", sig, ms, brief(result));
            return result;
        } catch (Throwable e) {
            long ms = (System.nanoTime() - start) / 1_000_000;
            log.warn("✖ {} {}ms 예외: {}", sig, ms, e.toString());
            throw e;                                      // 반드시 다시 던진다
        }
    }

    private String brief(Object o) {
        String s = String.valueOf(o);
        return (s.length() > 120) ? s.substring(0, 120) + "..." : s;
    }
}
