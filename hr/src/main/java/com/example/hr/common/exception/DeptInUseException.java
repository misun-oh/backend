package com.example.hr.common.exception;

/**
 * 소속 사원이 있는 부서를 삭제하려 할 때.
 * 정책 ①(설계 실습답안 문제6) — 삭제를 막고 안내 메시지를 보여준다.
 */
public class DeptInUseException extends RuntimeException {

    public DeptInUseException(String message) {
        super(message);
    }
}
