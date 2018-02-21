package com.agilion.domain.validators;

import com.agilion.domain.networkbuilder.TargetDeck;
import com.agilion.domain.networkbuilder.TargetDeckEntry;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by Alex_Lappy_486 on 2/19/18.
 */
public class TargetDeckValidator implements ConstraintValidator<ValidTargetDeck, TargetDeck> {

    @Override
    public void initialize(ValidTargetDeck constraintAnnotation) {

    }

    @Override
    public boolean isValid(TargetDeck value, ConstraintValidatorContext context) {
        boolean hasOnlyOneSelectorSource =  hasOneAndOnlyOneSeelctorSource(value, context);
        boolean allSelectorFieldsNotNull = allSelectorFieldsNotNull(value, context);

        return hasOnlyOneSelectorSource && allSelectorFieldsNotNull;
    }

    private boolean allSelectorFieldsNotNull(TargetDeck deck, ConstraintValidatorContext context)
    {
        boolean hasSelectorList = deck.getTargetDeckEntryList().size() >= 1
                && StringUtils.isNotBlank(deck.getTargetDeckEntryList().get(0).getSelectorList());

        if (hasSelectorList)
        {
            for (TargetDeckEntry entry : deck.getTargetDeckEntryList())
            {
                if (StringUtils.isBlank(entry.getSelectorList()) || entry.getSelectorType() == null)
                {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("{validation.project.targetdeck.entry.fieldisnull}").addConstraintViolation();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method returns true if the target has one, and only one, selector source. This means that the target deck
     * can have either a list of selectors, or a file containing selectors.
     *
     * @param deck
     * @return
     */
    private boolean hasOneAndOnlyOneSeelctorSource(TargetDeck deck, ConstraintValidatorContext context)
    {
        boolean hasSelectorList = deck.getTargetDeckEntryList().size() >= 1
                && StringUtils.isNotBlank(deck.getTargetDeckEntryList().get(0).getSelectorList());

        boolean hasSelectorFile = deck.getSelectorFile() != null;

        boolean valid = (!hasSelectorFile && hasSelectorList) || (hasSelectorFile && !hasSelectorList);
        if (!valid)
        {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{validation.project.targetdeckfail}").addConstraintViolation();
        }

        return valid;
    }
}