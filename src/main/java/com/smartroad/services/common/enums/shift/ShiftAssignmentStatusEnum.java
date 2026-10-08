package com.smartroad.services.common.enums.shift;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the lifecycle status of a worker's assignment to a shift template.
 * ACTIVE is the worker's current shift; ENDED is a closed, historical assignment
 * (effective_to has been set); INACTIVE is a assignment that was deliberately
 * suspended without yet being closed out.
 */
public enum ShiftAssignmentStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    INACTIVE("inactive"),
    ENDED("ended");

    private final String value;

    ShiftAssignmentStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ShiftAssignmentStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(ShiftAssignmentStatusEnum.class, value);
    }
}
