package com.smartroad.services.common.enums.machineutilization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents where a machine usage session's data originated from: a manual entry by an operator
 * or supervisor, or an automated device/telematics log.
 */
public enum UsageSourceEnum implements EnumValueHandler {

    MANUAL("manual"),
    DEVICE_LOG("device_log");

    private final String value;

    UsageSourceEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UsageSourceEnum fromValue(String value) {
        return EnumValueHandler.fromValue(UsageSourceEnum.class, value);
    }
}
