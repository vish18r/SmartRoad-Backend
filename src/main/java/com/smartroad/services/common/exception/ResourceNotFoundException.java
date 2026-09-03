package com.smartroad.services.common.exception;

public class ResourceNotFoundException extends SmartRoadException {

    public ResourceNotFoundException(String messageKey, Object... arguments) {
        super(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, messageKey, arguments);
    }
}
