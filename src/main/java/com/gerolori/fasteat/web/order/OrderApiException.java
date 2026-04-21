package com.gerolori.fasteat.web.order;

import org.springframework.http.HttpStatus;

public class OrderApiException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public OrderApiException(String errorCode, HttpStatus status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
