package com.example.hr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmpForm {

    private Long empId;   // 수정 시 채워짐

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 20, message = "이름은 20자 이내")
    private String empName;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 아닙니다")
    private String email;

    @NotNull(message = "부서를 선택하세요")
    private Long deptId;

    @NotNull
    @PositiveOrZero(message = "급여는 0 이상")
    private Integer salary;

    @NotNull(message = "입사일은 필수입니다")
    @PastOrPresent(message = "입사일은 미래일 수 없습니다")
    private LocalDate hireDate;
}
