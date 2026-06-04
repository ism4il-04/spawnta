package com.spawnta.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureWithMarginValidator.class)
@Documented
public @interface FutureWithMargin {
    String message() default "La date doit être dans le futur (avec une marge de 5 minutes)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Marge en minutes à ajouter à l'heure actuelle pour la validation
     */
    int marginMinutes() default 5;
}