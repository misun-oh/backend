package com.example.hr.common;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Constraint(validatedBy = HireDateAfterBirthValidator.class)
public @interface HireDateAfterBirth {
    String message() default "입사일이 생년월일보다 빠릅니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
