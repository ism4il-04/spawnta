package com.spawnta.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FutureWithMarginValidatorTest {

    private FutureWithMarginValidator validator;

    @Mock
    private FutureWithMargin annotation;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new FutureWithMarginValidator();
    }

    @Test
    void shouldReturnTrueForNullValue() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void shouldReturnTrueForFutureDate() {
        when(annotation.marginMinutes()).thenReturn(5);
        validator.initialize(annotation);

        LocalDateTime futureDate = LocalDateTime.now().plusHours(1);
        assertTrue(validator.isValid(futureDate, context));
    }

    @Test
    void shouldReturnTrueForDateWithinMargin() {
        when(annotation.marginMinutes()).thenReturn(5);
        validator.initialize(annotation);

        LocalDateTime dateWithinMargin = LocalDateTime.now().minusMinutes(3);
        assertTrue(validator.isValid(dateWithinMargin, context));
    }

    @Test
    void shouldReturnFalseForPastDateOutsideMargin() {
        when(annotation.marginMinutes()).thenReturn(5);
        validator.initialize(annotation);

        LocalDateTime pastDate = LocalDateTime.now().minusMinutes(10);
        assertFalse(validator.isValid(pastDate, context));
    }

    @Test
    void shouldReturnTrueForCurrentTimeWithMargin() {
        when(annotation.marginMinutes()).thenReturn(5);
        validator.initialize(annotation);

        LocalDateTime now = LocalDateTime.now();
        assertTrue(validator.isValid(now, context));
    }
}