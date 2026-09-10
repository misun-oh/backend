package com.example.hr.common.exception;

/** 존재하지 않는 리소스(사원·부서)를 조회했을 때. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
