package com.example.hr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Day 17 — 폼 로그인(세션) 기반 시큐리티 설정.
 * authorizeHttpRequests 는 위→아래 첫 매칭. 구체 규칙 먼저, anyRequest() 마지막.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/signup",
                                 "/css/**", "/js/**", "/uploads/**", "/error").permitAll()
                .requestMatchers("/depts/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/emps", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            // 폼 화면은 CSRF 유지(Thymeleaf 가 토큰 자동 삽입), REST/JWT 경로만 제외
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();   // 저장·검증 모두 이 인코더를 통해서만
    }
}
