package com.example.hr.dto;

public record StoredFile(String originName, String storedName, String path,
                         long size, String contentType) {}
