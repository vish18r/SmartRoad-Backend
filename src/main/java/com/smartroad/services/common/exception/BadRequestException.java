package com.smartroad.services.common.exception;

public class BadRequestException extends SmartRoadException {

    public BadRequestException(String messageKey, Object... arguments) {
        super(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_INVALID_INPUT, messageKey, arguments);
    }
}
