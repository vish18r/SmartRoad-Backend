package com.smartroad.services.common.enums.workers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents whether a worker has a registered face and may therefore use face check-in.
 */
public enum WorkerFaceStatusEnum implements EnumValueHandler {

    FACE_REGISTERED("FACE_REGISTERED"),

    FACE_NOT_REGISTERED("FACE_NOT_REGISTERED");

    private final String value;

    WorkerFaceStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static WorkerFaceStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(WorkerFaceStatusEnum.class, value);
    }
}
