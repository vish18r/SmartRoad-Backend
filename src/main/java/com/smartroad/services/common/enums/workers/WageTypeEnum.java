package com.smartroad.services.common.enums.workers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents how a worker's wage is structured: a fixed amount per worked day, or a fixed
 * amount per calendar month.
 */
public enum WageTypeEnum implements EnumValueHandler {

    DAILY("daily"),
    MONTHLY("monthly");

    private final String value;

    WageTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static WageTypeEnum fromValue(String value) {
        return EnumValueHandler.fromValue(WageTypeEnum.class, value);
    }
}
