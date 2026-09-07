package com.example.hr.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice        // 화면(뷰) 반환용. REST는 @RestControllerAdvice
public class GlobalExceptionHandler {

    // 없는 리소스 → 404 화면
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(NoSuchElementException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/404";
    }

    // 업무 규칙 위반 → 이전 화면 + 메시지 (예: 부서에 사원이 있어 삭제 불가)
    @ExceptionHandler(BusinessException.class)
    public String business(BusinessException e, RedirectAttributes ra) {
        ra.addFlashAttribute("error", e.getMessage());
        return "redirect:" + e.getReturnUrl();
    }

    // 그 외 전부 → 500 화면 + 로그
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String etc(Exception e, Model model) {
        log.error("처리되지 않은 예외", e);
        model.addAttribute("message", "잠시 후 다시 시도해 주세요");
        return "error/500";
    }
}
