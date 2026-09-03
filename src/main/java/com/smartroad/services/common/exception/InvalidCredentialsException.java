package com.smartroad.services.common.exception;

public class InvalidCredentialsException extends AuthenticationException {

    public InvalidCredentialsException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
