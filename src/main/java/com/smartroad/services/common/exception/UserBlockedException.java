package com.smartroad.services.common.exception;

public class UserBlockedException extends AuthenticationException {

    public UserBlockedException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
