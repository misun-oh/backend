package com.example.hr.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Day 14 심화 4절 — @Around 로 재시도 애스펙트를 만들기 위한 커스텀 애노테이션.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retryable {
    int max() default 3;
    Class<? extends Throwable> on() default Exception.class;
}
