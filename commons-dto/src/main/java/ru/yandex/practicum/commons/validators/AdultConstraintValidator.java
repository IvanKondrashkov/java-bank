package ru.yandex.practicum.commons.validators;

import java.time.Period;
import java.time.LocalDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.commons.annotations.AdultConstraint;

public class AdultConstraintValidator implements ConstraintValidator<AdultConstraint, LocalDate> {
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return Period.between(value, LocalDate.now()).getYears() >= 18;
    }
}