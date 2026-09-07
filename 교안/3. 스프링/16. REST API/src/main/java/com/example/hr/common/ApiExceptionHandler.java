package com.example.hr.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice(basePackages = "com.example.hr.controller")   // API 컨트롤러 대상
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validation(MethodArgumentNotValidException e) {
        Map<String, String> fe = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
         .forEach(x -> fe.putIfAbsent(x.getField(), x.getDefaultMessage()));
        return new ApiError("VALIDATION_ERROR", "입력값을 확인하세요", fe);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(NoSuchElementException e) {
        return ApiError.of("NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)     // 커스텀 런타임 예외
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError business(BusinessException e) {
        return ApiError.of(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError etc(Exception e) {
        log.error("API 예외", e);
        return ApiError.of("INTERNAL_ERROR", "서버 오류");
    }
}
