package com.nextenti.services.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCodeMapping {

    DAO_NOT_FOUND("404.100", HttpStatus.NOT_FOUND),
    SERVICE_INVALID_INPUT("400.100", HttpStatus.BAD_REQUEST),
    SERVICE_VALIDATION_FAILED("400.101", HttpStatus.BAD_REQUEST),
    SERVICE_UNAUTHORIZED("401.100", HttpStatus.UNAUTHORIZED),
    SERVICE_ACCESS_DENIED("403.100", HttpStatus.FORBIDDEN),
    SERVICE_INTERNAL_ERROR("500.100", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus httpStatus;

    ErrorCodeMapping(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
