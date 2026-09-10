package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/** 부서 추가·수정 폼 — depts.html 모달과 1:1. */
@Getter
@Setter
public class DeptForm {

    private Long deptId;

    @NotBlank(message = "부서명을 입력하세요.")
    private String deptName;

    private String location;

    public boolean isNew() {
        return deptId == null;
    }
}
