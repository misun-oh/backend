package com.example.hr.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Day 14 실습답안 문제 3 — 특정 메서드만 시간 측정하기 위한 커스텀 애노테이션.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timed {
    int warnMs() default 100;
}
