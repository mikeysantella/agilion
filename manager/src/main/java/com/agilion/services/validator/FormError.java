package com.agilion.services.validator;

/**
 * Created by Alex_Lappy_486 on 2/19/18.
 */
public class FormError
{
    private String objectPath;
    private String errorMessage;
    public static final String GLOBAL_PATH = "_global";

    public String getObjectPath() {
        return objectPath;
    }

    public void setObjectPath(String objectPath) {
        this.objectPath = objectPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public FormError(String objectPath, String errorMessage) {
        this.objectPath = objectPath;
        this.errorMessage = errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static FormError createFormErrorFromMessage(String msg)
    {
        FormError error = new FormError(GLOBAL_PATH, msg);
        return error;

    }
}
