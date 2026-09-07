package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 부록 3. 회원가입 폼.
 */
public record SignupForm(
        @NotBlank @Size(min = 4, max = 20) String memberId,
        @NotBlank @Size(min = 4) String password,
        @NotBlank String memberName) {
}
