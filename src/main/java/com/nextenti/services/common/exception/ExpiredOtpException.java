package com.nextenti.services.common.exception;

public class ExpiredOtpException extends AuthenticationException {

    public ExpiredOtpException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
