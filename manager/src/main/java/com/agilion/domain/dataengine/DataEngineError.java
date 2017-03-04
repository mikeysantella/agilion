package com.agilion.domain.dataengine;

/**
 * Created by Alex_Lappy_486 on 3/4/17.
 */
public class DataEngineError
{
    private String errorMessage;
    private Throwable exception;

    public DataEngineError()
    {

    }

    public DataEngineError(String errorMessage, Throwable exception) {
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
