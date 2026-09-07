package com.example.hr.common;

import com.example.hr.dto.EmpForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HireDateAfterBirthValidator implements ConstraintValidator<HireDateAfterBirth, EmpForm> {
    public boolean isValid(EmpForm f, ConstraintValidatorContext ctx) {
        if (f.getBirth() == null || f.getHireDate() == null) return true;   // null은 @NotNull이 담당
        return !f.getHireDate().isBefore(f.getBirth());
    }
}
