package com.nextenti.services.common.exception;

public class AuthenticationException extends SmartRoadException {

    public AuthenticationException(String messageKey, Object... arguments) {
        super(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_INVALID_INPUT, messageKey, arguments);
    }
}
