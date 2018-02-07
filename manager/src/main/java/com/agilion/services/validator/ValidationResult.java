package com.agilion.services.validator;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.io.Serializable;
import java.util.List;

public class ValidationResult implements Serializable
{
    private List<FieldError> fieldErrors;

    private List<ObjectError> globalErrors;

    public ValidationResult()
    {

    }

    public ValidationResult(BindingResult result)
    {
        this.fieldErrors = result.getFieldErrors();
        this.globalErrors = result.getGlobalErrors();
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public List<ObjectError> getGlobalErrors() {
        return globalErrors;
    }

    public void setGlobalErrors(List<ObjectError> globalErrors) {
        this.globalErrors = globalErrors;
    }
}
