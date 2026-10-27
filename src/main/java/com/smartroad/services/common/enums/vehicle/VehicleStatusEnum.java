package com.smartroad.services.common.enums.vehicle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the operational status of a vehicle.
 */
public enum VehicleStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    MAINTENANCE("maintenance"),
    INACTIVE("inactive");

    private final String value;

    VehicleStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static VehicleStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(VehicleStatusEnum.class, value);
    }
}
