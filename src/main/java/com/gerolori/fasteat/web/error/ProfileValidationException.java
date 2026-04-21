package com.gerolori.fasteat.web.error;

import org.springframework.http.HttpStatus;

public class ProfileValidationException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;
    private final Object details;

    public ProfileValidationException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.UNPROCESSABLE_ENTITY, null);
    }

    public ProfileValidationException(String errorCode, String message, HttpStatus status, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object getDetails() {
        return details;
    }
}
