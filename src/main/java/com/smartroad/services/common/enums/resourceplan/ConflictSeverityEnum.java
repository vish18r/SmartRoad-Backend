package com.smartroad.services.common.enums.resourceplan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents how severe a detected resource-planning conflict is.
 */
public enum ConflictSeverityEnum implements EnumValueHandler {

    BLOCKING("blocking"),
    WARNING("warning");

    private final String value;

    ConflictSeverityEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ConflictSeverityEnum fromValue(String value) {
        return EnumValueHandler.fromValue(ConflictSeverityEnum.class, value);
    }
}
