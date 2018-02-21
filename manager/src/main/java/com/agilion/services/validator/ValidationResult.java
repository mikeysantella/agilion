package com.agilion.services.validator;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ValidationResult implements Serializable
{
    private List<FormError> errors;
    public final String OBJ_ID = "VALIDATION_RESULT";

    public ValidationResult()
    {
        errors = new LinkedList<>();
    }

    public ValidationResult(BindingResult result)
    {
        errors = new LinkedList<>();
        for (FieldError error : result.getFieldErrors())
        {
            FormError formError = new FormError(error.getField(), error.getDefaultMessage());
            this.errors.add(formError);
        }
    }

    public List<FormError> getErrors() {
        return errors;
    }

    public void setErrors(List<FormError> errors) {
        this.errors = errors;
    }

    public void addError(FormError error)
    {
        this.errors.add(error);
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }
}
