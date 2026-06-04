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
        // Since the app uses timezone-naive LocalDateTime, allow a timezone-safe margin
        // of up to 14 hours to accommodate users in timezones behind the server (e.g. UTC, US/UTC-X)
        LocalDateTime minimumTime = now.minusHours(14).minusMinutes(marginMinutes);
        
        return value.isAfter(minimumTime);
    }
}