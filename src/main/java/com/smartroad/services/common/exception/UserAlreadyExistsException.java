package com.smartroad.services.common.exception;

public class UserAlreadyExistsException extends BadRequestException {

    public UserAlreadyExistsException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
