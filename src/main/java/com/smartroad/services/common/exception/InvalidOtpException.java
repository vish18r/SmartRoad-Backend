package com.smartroad.services.common.exception;

public class InvalidOtpException extends AuthenticationException {

    public InvalidOtpException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
