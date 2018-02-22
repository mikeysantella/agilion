package com.agilion.domain.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Created by Alex_Lappy_486 on 2/19/18.
 */
@Documented
@Constraint(validatedBy = TargetDeckValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTargetDeck
{
    String message() default "A Target Deck must have a selector list, or a selector file (but not both)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
