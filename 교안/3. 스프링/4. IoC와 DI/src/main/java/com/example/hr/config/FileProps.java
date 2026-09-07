package com.example.hr.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Day 4 심화 — @Value 를 여러 개 쓰는 대신 타입 안전하게 묶어 받는 설정 바인딩 예시.
 * application.yml:
 *   hr:
 *     file:
 *       base-dir: /var/hr/upload
 *       max-size: 10485760
 *       allowed-ext: [png, jpg, pdf]
 */
@ConfigurationProperties(prefix = "hr.file")
@Component
public record FileProps(String baseDir, long maxSize, List<String> allowedExt) {
}
