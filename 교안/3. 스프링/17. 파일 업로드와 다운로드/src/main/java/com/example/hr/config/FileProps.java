package com.example.hr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hr.file")
public record FileProps(String baseDir) {}
