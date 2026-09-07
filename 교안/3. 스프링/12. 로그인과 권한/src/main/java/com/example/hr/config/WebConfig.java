package com.example.hr.config;

import com.example.hr.interceptor.AdminOnlyInterceptor;
import com.example.hr.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Day 12 — 본문 6절. 인터셉터 등록. 순서 중요(로그인 → 권한).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .excludePathPatterns("/login", "/logout", "/css/**", "/js/**", "/images/**");

        registry.addInterceptor(new AdminOnlyInterceptor())
                .addPathPatterns("/depts/**", "/emps/*/delete");
    }
}
