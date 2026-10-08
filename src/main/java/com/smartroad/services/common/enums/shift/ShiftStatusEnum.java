package com.smartroad.services.common.enums.shift;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the lifecycle status of a shift template.
 */
public enum ShiftStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    ShiftStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ShiftStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(ShiftStatusEnum.class, value);
    }
}
