package com.nextenti.services.common.exception;

public class UserBlockedException extends AuthenticationException {

    public UserBlockedException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
