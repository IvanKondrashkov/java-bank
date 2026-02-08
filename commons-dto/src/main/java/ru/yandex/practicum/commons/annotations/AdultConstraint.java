package ru.yandex.practicum.commons.annotations;

import java.lang.annotation.*;
import jakarta.validation.Payload;
import jakarta.validation.Constraint;
import ru.yandex.practicum.commons.validators.AdultConstraintValidator;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AdultConstraintValidator.class)
@Documented
public @interface AdultConstraint {
    String message() default "Incorrect person age!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}