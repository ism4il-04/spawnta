package com.spawnta.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class FutureWithMarginValidator implements ConstraintValidator<FutureWithMargin, LocalDateTime> {
    
    private int marginMinutes;
    
    @Override
    public void initialize(FutureWithMargin constraintAnnotation) {
        this.marginMinutes = constraintAnnotation.marginMinutes();
    }
    
    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minimumTime = now.minusMinutes(marginMinutes);
        
        return value.isAfter(minimumTime);
    }
}