package com.smartroad.services.common.enums.machine;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the operational status of a machine.
 */
public enum MachineStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    MAINTENANCE("maintenance"),
    INACTIVE("inactive"),
    RETIRED("retired");

    private final String value;

    MachineStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MachineStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(MachineStatusEnum.class, value);
    }
}
